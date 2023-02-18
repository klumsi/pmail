import Vue from 'vue'
import App from './App.vue'
import global from './common'
import router from './router'
import TDesign from 'tdesign-vue';
import 'tdesign-vue/es/style/index.css';
import CKEditor from '@ckeditor/ckeditor5-vue2'
import axios from 'axios';

Vue.use(TDesign);
Vue.use(CKEditor)

Vue.config.productionTip = false

Vue.prototype.GLOBAL = global
Vue.prototype.getUsername = () => {
    try {
        const token = localStorage.getItem("token");
        const payload = JSON.parse(atob(token.split('.')[1]));
        const sub = payload['sub'];
        return sub;
    } catch {
        return '';
    }
}
Vue.prototype.getFolder = function () {
    try {
        const patt = /\/[a-zA-Z0-9]{1,}/;
        const folder = patt.exec(this.$route.path)[0];
        return folder.substring(1);
    } catch {
        return '';
    }
}

router.beforeEach((to, from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title;
    }
    next()
})

axios.interceptors.request.use(config => {
    if (localStorage.getItem('token')) {
        config.headers.Authorization = localStorage.getItem('token')
    } else {
        router.replace('/login')
    }
    return config
})

new Vue({
    router,
    render: h => h(App)
}).$mount('#app')
