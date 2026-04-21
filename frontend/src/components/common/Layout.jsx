import {Outlet} from 'react-router-dom'
import Header from './Header'
import Sidebar from './Sidebar'
import styles from './Layout.module.css'

function Layout() {
  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.body}>
        <Sidebar />
        <main className={styles.main}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default Layout
