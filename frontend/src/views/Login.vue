<template>
    <div class="login-container">
        <h2>帐号登录</h2>
        <t-form :data="formData" @submit="login">
            <t-form-item name="username">
                <t-input v-model.trim="formData.username" placeholder="请输入邮箱帐号" :suffix="'@' + this.GLOBAL.DOMAIN">
                    <UserIcon slot="prefix-icon"></UserIcon>
                </t-input>
            </t-form-item>
            <t-form-item name="password">
                <t-input type="password" clearable v-model.trim="formData.password" placeholder="请输入密码">
                    <LockOnIcon slot="prefix-icon"></LockOnIcon>
                </t-input>
            </t-form-item>
            <t-form-item>
                <t-checkbox v-model="formData.stayLoggedIn">保持登录</t-checkbox>
                <a href="javascript:;" @click="toRegister">注册新帐号</a>
            </t-form-item>
            <t-form-item>
                <t-button theme="primary" type="submit" block>登录</t-button>
            </t-form-item>
        </t-form>
    </div>
</template>

<script>
import { LockOnIcon, UserIcon } from 'tdesign-icons-vue';
import axios from 'axios';

export default {
    components: {
        LockOnIcon,
        UserIcon
    },
    data() {
        return {
            formData: {
                username: '',
                password: '',
                stayLoggedIn: true
            },
        };
    },

    methods: {
        login() {
            if (this.formData.username === '' || this.formData.password === '') {
                this.$message.warning('用户名或密码为空');
                return;
            }
            axios.post(this.GLOBAL.SERVER + '/login', {
                user: {
                    username: this.formData.username,
                    password: this.formData.password
                },
                stayLoggedIn: this.formData.stayLoggedIn
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('登录成功');
                        localStorage.setItem('token', res.data.data);
                        setTimeout(() => {
                            this.$router.replace('/');
                        }, 1);
                    } else {
                        this.$message.warning('用户名或密码错误');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        toRegister() {
            this.$router.replace('/register');
        }
    },
};
</script>

<style lang="less" scoped>
.login-container {
    margin-left: 10px;
    margin-right: 10px;
    background-color: white;
    height: 350px;
    width: 480px;
    margin-top: calc(-8%);
    border-radius: 5px;
    box-shadow: 0px 2px 5px rgb(196, 196, 196);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.t-form {
    width: 80%;
    margin-top: 20px;
    margin-bottom: 20px;
}

a {
    color: black;
}

/deep/ .t-form__controls-content {
    justify-content: space-between;
}
</style>