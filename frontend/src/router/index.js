import Vue from 'vue'
import VueRouter from 'vue-router'

import Login from '@/views/Login'
import Register from '@/views/Register'
import Compose from '@/views/board/Compose'
import Contacts from '@/views/board/Contacts'
import Inbox from '@/views/board/Inbox'
import Archive from '@/views/board/Archive'
import Drafts from '@/views/board/Drafts'
import Junk from '@/views/board/Junk'
import Sent from '@/views/board/Sent'
import Trash from '@/views/board/Trash'
import Mail from '@/views/board/Mail'

Vue.use(VueRouter)

const router = new VueRouter({
    routes: [
        {
            path: '/',
            redirect: '/inbox'
        },
        {
            path: '/login',
            component: Login,
        },
        {
            path: '/register',
            component: Register,
        },
        {
            path: '/archive',
            component: Archive,
            props: true,
        },
        {
            path: '/compose',
            component: Compose,
            props: true,
            meta: {
                keepAlive: true,
            }
        },
        {
            path: '/contacts',
            component: Contacts,
            props: true,
        },
        {
            path: '/drafts',
            component: Drafts,
            props: true,
        },
        {
            path: '/inbox',
            component: Inbox,
            props: true,
        },
        {
            path: '/junk',
            component: Junk,
            props: true,
        },
        {
            path: '/sent',
            component: Sent,
            props: true,
        },
        {
            path: '/trash',
            component: Trash,
            props: true
        },
        {
            path: '/archive/:id',
            component: Mail,
            props: true
        },
        {
            path: '/compose/:id',
            component: Mail,
            props: true
        },
        {
            path: '/contacts/:id',
            component: Mail,
            props: true
        },
        {
            path: '/drafts/:id',
            component: Mail,
            props: true
        },
        {
            path: '/inbox/:id',
            component: Mail,
            props: true
        },
        {
            path: '/junk/:id',
            component: Mail,
            props: true
        },
        {
            path: '/sent/:id',
            component: Mail,
            props: true
        },
        {
            path: '/trash/:id',
            component: Mail,
            props: true
        },
    ]
})

const checkToken = function (token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const sub = payload['sub'];
        const exp = payload['exp'];
        const now = parseInt(new Date().getTime() / 1000);
        return now < exp;
    } catch (e) {
        return false;
    }
}

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    if (!checkToken(token) && to.path !== '/login' && to.path !== '/register') {
        localStorage.removeItem('token');
        next('/login');
    } else {
        next();
    }
})


export default router;
