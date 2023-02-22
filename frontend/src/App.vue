<template>
    <div class="app-container">
        <Header :isLoggedIn="isLoggedIn" :userInfo="userInfo" :visSearch="visSearch" :headStyle="headStyle"></Header>
        <div v-if="isLoggedIn" class="board">
            <div class="aside">
                <Aside :active="asideActive"></Aside>
            </div>
            <div class="content">
                <keep-alive>
                    <router-view v-if="$route.meta.keepAlive" :userInfo="userInfo"></router-view>
                </keep-alive>
                <router-view v-if="!$route.meta.keepAlive"></router-view>
            </div>
        </div>
        <div v-else class="blank-board">
            <router-view></router-view>
        </div>
    </div>
</template>

<script>
import router from './router'
import bus from '@/EventBus'
import Header from '@/components/Header'
import Aside from '@/components/Aside'
import axios from 'axios'

export default {
    name: 'App',
    data() {
        return {
            isLoggedIn: true,
            asideActive: '',
            userInfo: {
                username: '',
                nickname: ''
            },
            visSearch: true,
            headStyle: 'min-width: 1238px;'
        }
    },
    components: {
        router,
        Header,
        Aside,
    },
    methods: {
        init() {
            const patt = /\/[a-zA-Z0-9]{1,}/
            const asideActive = patt.exec(this.$route.path)[0]
            this.asideActive = asideActive;
            this.isLoggedIn = localStorage.getItem('token') !== null
        }
    },
    mounted() {
        document.querySelector('html').setAttribute('style', 'height: 100%')
        document.querySelector('body').setAttribute('style', 'background-color:#f0f0f0; margin: 0px; height: 100%; font-family: -apple-system,BlinkMacSystemFont,PingFang SC,Microsoft YaHei,sans-serif;')
    },
    created() {
        if (this.getFolder() === 'login' || this.getFolder() == 'register') {
            this.headStyle = ''
        } else {
            this.headStyle = 'min-width: 1238px;'
        }
        bus.$emit('search', '');
        if (this.getFolder() === 'compose') {
            this.visSearch = false;
        } else {
            this.visSearch = true;
        }
        const patt = /\/[a-zA-Z0-9]{1,}/;
        const asideActive = patt.exec(this.$route.path)[0]
        const token = localStorage.getItem('token');
        this.asideActive = asideActive;
        this.isLoggedIn = token !== null;
        if (token) {
            this.userInfo.username = JSON.parse(atob(token.split('.')[1]))['sub'];
            axios.get(this.GLOBAL.SERVER + '/user/' + this.userInfo.username).then(res => {
                if (res.data.msg === 'authentication failed') {
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.userInfo.nickname = res.data.data.nickname;
                    } else {
                        this.$message.warning('获取用户信息失败');
                    }
                }

            }).catch(error => {
                this.$message.error('服务器错误');
            })
        }
    },
    updated() {
        if (this.getFolder() === 'login' || this.getFolder() == 'register') {
            this.headStyle = ''
        } else {
            this.headStyle = 'min-width: 1238px;'
        }
        bus.$emit('search', '');
        if (this.getFolder() === 'compose') {
            this.visSearch = false;
        } else {
            this.visSearch = true;
        }
        const patt = /\/[a-zA-Z0-9]{1,}/;
        const asideActive = patt.exec(this.$route.path)[0];
        const token = localStorage.getItem('token');
        this.asideActive = asideActive;
        this.isLoggedIn = token !== null;
        if (token) {
            this.userInfo.username = JSON.parse(atob(token.split('.')[1]))['sub'];
            if (this.userInfo.nickname !== '') return;
            axios.get(this.GLOBAL.SERVER + '/user/' + this.userInfo.username).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.userInfo.nickname = res.data.data.nickname;
                    } else {
                        this.$message.warning('获取用户信息失败');
                    }
                }

            }).catch(error => {
                this.$message.error('服务器错误');
            })
        }
    }
}
</script>

<style lang="less" scoped>
::-webkit-scrollbar {
    display: none;
}

.app-container {
    height: 100%;
}

.board {
    height: calc(100% - 64px);
    display: flex;

    .aside {
        height: 100%;
    }
}

.content {
    min-width: 1000px;
    height: calc(100% - 12px);
    width: 100%;
    background-color: white;
    border: 6px solid #f0f0f0;
    border-left: none;
    overflow-y: scroll;
}

.blank-board {
    height: calc(100% - 64px);
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>