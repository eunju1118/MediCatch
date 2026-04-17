// ─────────────────────────────────────────────────────────────────────────────
// Medicatch Platform — Jenkins 배포 파이프라인
//
// 전제조건:
//   - Jenkins Credentials:
//       'docker-hub-creds'  : Username+Password (Docker Hub)
//       'deploy-ssh-key'    : SSH Private Key (배포 서버)
//   - Jenkins Plugins: Pipeline, SSH Agent, Credentials Binding
//   - 배포 서버에 Docker + docker compose v2 설치
// ─────────────────────────────────────────────────────────────────────────────
pipeline {
    agent any

    // ── 파라미터 ──────────────────────────────────────────────────────────────
    parameters {
        string(
            name:         'IMAGE_TAG',
            defaultValue: 'latest',
            description:  '배포할 Docker 이미지 태그 (예: latest, abc1234)'
        )
        string(
            name:         'DEPLOY_HOST',
            defaultValue: "${env.DEFAULT_DEPLOY_HOST ?: 'deploy-server'}",
            description:  '배포 대상 서버 주소'
        )
        string(
            name:         'DEPLOY_USER',
            defaultValue: 'deploy',
            description:  '배포 서버 SSH 사용자'
        )
        booleanParam(
            name:         'ROLLBACK',
            defaultValue: false,
            description:  '이전 이미지(previous 태그)로 롤백'
        )
    }

    // ── 환경 변수 ─────────────────────────────────────────────────────────────
    environment {
        DOCKER_CREDS  = credentials('docker-hub-creds')
        DOCKER_USER   = "${DOCKER_CREDS_USR}"
        DOCKER_PASS   = "${DOCKER_CREDS_PSW}"
        IMG           = "${DOCKER_CREDS_USR}/medicatch"
        TAG           = "${params.ROLLBACK ? 'previous' : params.IMAGE_TAG}"
        DEPLOY        = "${params.DEPLOY_USER}@${params.DEPLOY_HOST}"

        // 배포 순서에 따른 서비스 목록
        ORDERED_SERVICES = "eureka-server api-gateway health-service insurance-service chat-service frontend"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
    }

    // ═══════════════════════════════════════════════════════════════════════════
    stages {

        // ── 0. 파이프라인 검증 ────────────────────────────────────────────────
        stage('Validate') {
            steps {
                script {
                    echo "▶ 배포 정보"
                    echo "  IMAGE_TAG : ${TAG}"
                    echo "  DEPLOY_HOST: ${params.DEPLOY_HOST}"
                    echo "  ROLLBACK  : ${params.ROLLBACK}"

                    if (!params.DEPLOY_HOST?.trim()) {
                        error("DEPLOY_HOST 파라미터가 비어 있습니다.")
                    }
                }
            }
        }

        // ── 1. 이미지 Pull (병렬) ─────────────────────────────────────────────
        stage('Pull Images') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin

                            echo "=== Pulling images (tag: ${TAG}) ==="
                            docker pull ${IMG}-eureka-server:${TAG}    &
                            docker pull ${IMG}-api-gateway:${TAG}      &
                            docker pull ${IMG}-health-service:${TAG}   &
                            docker pull ${IMG}-insurance-service:${TAG} &
                            docker pull ${IMG}-chat-service:${TAG}     &
                            docker pull ${IMG}-frontend:${TAG}         &
                            wait
                            echo "=== All images pulled ==="
                        '
                    """
                }
            }
        }

        // ── 2. 네트워크 & 인프라 준비 ────────────────────────────────────────
        stage('Prepare Network') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            docker network inspect medicatch-network >/dev/null 2>&1 || \
                                docker network create --driver bridge medicatch-network
                            echo "medicatch-network ready"
                        '
                    """
                }
            }
        }

        // ── 3. Eureka Server 배포 ────────────────────────────────────────────
        stage('Deploy: Eureka Server') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            ${deployCommand("eureka-server", "medicatch-eureka", "8761:8761", "")}
                        '
                    """
                }
                script {
                    waitForHealth('8761', 'Eureka Server')
                }
            }
        }

        // ── 4. API Gateway 배포 ──────────────────────────────────────────────
        stage('Deploy: API Gateway') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            ${deployCommand("api-gateway", "medicatch-gateway", "8080:8080",
                                "-e JWT_SECRET=\\\${JWT_SECRET:-medicatch-secret-key-must-be-at-least-256-bits-long} " +
                                "-e EUREKA_URL=http://medicatch-eureka:8761/eureka/")}
                        '
                    """
                }
                script {
                    waitForHealth('8080', 'API Gateway')
                }
            }
        }

        // ── 5. Application Services 배포 (병렬) ──────────────────────────────
        stage('Deploy: Application Services') {
            parallel {
                stage('health-service') {
                    steps {
                        sshagent(['deploy-ssh-key']) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                                    ${deployCommand("health-service", "medicatch-health", "8081:8081",
                                        "-e DB_URL=\\\${HEALTH_DB_URL} " +
                                        "-e DB_USERNAME=\\\${DB_USERNAME} " +
                                        "-e DB_PASSWORD=\\\${DB_PASSWORD} " +
                                        "-e CODEF_CLIENT_ID=\\\${CODEF_CLIENT_ID} " +
                                        "-e CODEF_CLIENT_SECRET=\\\${CODEF_CLIENT_SECRET} " +
                                        "-e CODEF_PUBLIC_KEY=\\\${CODEF_PUBLIC_KEY} " +
                                        "-e CODEF_MODE=\\\${CODEF_MODE:-DEMO} " +
                                        "-e OPENAI_API_KEY=\\\${OPENAI_API_KEY} " +
                                        "-e EUREKA_URL=http://medicatch-eureka:8761/eureka/")}
                                '
                            """
                        }
                    }
                }
                stage('insurance-service') {
                    steps {
                        sshagent(['deploy-ssh-key']) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                                    ${deployCommand("insurance-service", "medicatch-insurance", "8082:8082",
                                        "-e DB_URL=\\\${INSURANCE_DB_URL} " +
                                        "-e DB_USERNAME=\\\${DB_USERNAME} " +
                                        "-e DB_PASSWORD=\\\${DB_PASSWORD} " +
                                        "-e CODEF_CLIENT_ID=\\\${CODEF_CLIENT_ID} " +
                                        "-e CODEF_CLIENT_SECRET=\\\${CODEF_CLIENT_SECRET} " +
                                        "-e CODEF_PUBLIC_KEY=\\\${CODEF_PUBLIC_KEY} " +
                                        "-e CODEF_MODE=\\\${CODEF_MODE:-DEMO} " +
                                        "-e OPENAI_API_KEY=\\\${OPENAI_API_KEY} " +
                                        "-e EUREKA_URL=http://medicatch-eureka:8761/eureka/")}
                                '
                            """
                        }
                    }
                }
                stage('chat-service') {
                    steps {
                        sshagent(['deploy-ssh-key']) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                                    ${deployCommand("chat-service", "medicatch-chat", "8085:8085",
                                        "-e OPENAI_API_KEY=\\\${OPENAI_API_KEY} " +
                                        "-e EUREKA_URL=http://medicatch-eureka:8761/eureka/")}
                                '
                            """
                        }
                    }
                }
            }
        }

        // ── 6. Application Services 헬스체크 ─────────────────────────────────
        stage('Health Check: Services') {
            parallel {
                stage('health-service ready') {
                    steps { script { waitForHealth('8081', 'Health Service') } }
                }
                stage('insurance-service ready') {
                    steps { script { waitForHealth('8082', 'Insurance Service') } }
                }
                stage('chat-service ready') {
                    steps { script { waitForHealth('8085', 'Chat Service') } }
                }
            }
        }

        // ── 7. Frontend 배포 ─────────────────────────────────────────────────
        stage('Deploy: Frontend') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            docker stop medicatch-frontend 2>/dev/null || true
                            docker rm   medicatch-frontend 2>/dev/null || true
                            docker run -d \\
                                --name    medicatch-frontend \\
                                --restart unless-stopped \\
                                --network medicatch-network \\
                                -p 3000:80 \\
                                ${IMG}-frontend:${TAG}
                            echo "Frontend deployed"
                        '
                    """
                }
            }
        }

        // ── 8. 전체 헬스체크 요약 ────────────────────────────────────────────
        stage('Final Health Check') {
            steps {
                sshagent(['deploy-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY} '
                            echo ""
                            echo "=== Container Status ==="
                            docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}" \\
                                --filter "name=medicatch-"
                        '
                    """
                }
            }
        }

    } // stages

    // ── Post Actions ──────────────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════╗
║   ✅  Medicatch 배포 완료               ║
║   TAG: ${TAG}
╚══════════════════════════════════════════╝"""
        }
        failure {
            echo """
╔══════════════════════════════════════════╗
║   ❌  배포 실패 — 로그를 확인하세요     ║
╚══════════════════════════════════════════╝"""
        }
        always {
            script {
                // Docker 로그인 정보 정리
                sshagent(['deploy-ssh-key']) {
                    sh "ssh -o StrictHostKeyChecking=no ${DEPLOY} 'docker logout' || true"
                }
            }
        }
    }

} // pipeline

// ─────────────────────────────────────────────────────────────────────────────
// Helper: docker stop/rm/run 명령 생성
// ─────────────────────────────────────────────────────────────────────────────
String deployCommand(String svcName, String containerName, String ports, String envVars) {
    return """
        echo "--- Deploying ${svcName} ---"
        docker stop ${containerName} 2>/dev/null || true
        docker rm   ${containerName} 2>/dev/null || true
        docker run -d \\
            --name    ${containerName} \\
            --restart unless-stopped \\
            --network medicatch-network \\
            -p ${ports} \\
            ${envVars} \\
            ${IMG}-${svcName}:${TAG}
        echo "${svcName} started"
    """.stripIndent()
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: 서비스가 healthy 상태가 될 때까지 대기 (최대 3분)
// ─────────────────────────────────────────────────────────────────────────────
void waitForHealth(String port, String label) {
    echo "⏳ ${label} 헬스체크 대기 중... (port ${port})"
    timeout(time: 3, unit: 'MINUTES') {
        waitUntil(initialRecurrencePeriod: 5000, quiet: true) {
            def result = sh(
                script: "curl -sf http://${params.DEPLOY_HOST}:${port}/actuator/health || exit 1",
                returnStatus: true
            )
            return result == 0
        }
    }
    echo "✅ ${label} is healthy"
}
