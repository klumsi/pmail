<template>
    <div class="register-container">
        <h2>账号注册</h2>
        <t-form :data="formData" :rules="rules" @submit="register" @reset="reset" labelAlign="left" labelWidth="85px"
            :requiredMark="false">
            <t-form-item label="邮箱帐号" name="username">
                <t-popup content="3~15个字符，可使用字母、数字、下划线，需要以字母开头" destroyOnClose trigger="focus">
                    <t-input v-model.trim="formData.username" placeholder="邮箱地址" :suffix="'@' + this.GLOBAL.DOMAIN">
                        <UserIcon slot="prefix-icon"></UserIcon>
                    </t-input>
                </t-popup>
            </t-form-item>
            <t-form-item label="密码" name="password">
                <t-popup content="6~16个字符" destroyOnClose trigger="focus">
                    <t-input type="password" clearable v-model.trim="formData.password" placeholder="密码">
                        <LockOnIcon slot="prefix-icon"></LockOnIcon>
                    </t-input>
                </t-popup>
            </t-form-item>
            <t-form-item label="确认密码" name="confirmPassword">
                <t-popup content="6~16个字符" destroyOnClose trigger="focus">
                    <t-input type="password" clearable v-model.trim="formData.confirmPassword" placeholder="确认密码">
                        <LockOnIcon slot="prefix-icon"></LockOnIcon>
                    </t-input>
                </t-popup>
            </t-form-item>
            <t-form-item>
                <t-button theme="default" type="reset" block>重置</t-button>
                <t-button id="submit" theme="primary" type="submit" block>注册</t-button>
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
        UserIcon,
    },
    data() {
        return {
            formData: {
                username: '',
                password: '',
                confirmPassword: '',
            },
            rules: {
                username: [{ required: true }, {
                    validator: val => /^[a-zA-z_][a-zA-z0-9_]{2,14}$/.test(val),
                    message: '邮箱帐号格式错误'
                }],
                password: [{ required: true }, {
                    validator: val => /^[^]{6,16}$/.test(val),
                    message: '密码格式错误'
                }],
                confirmPassword: [{ required: true }, {
                    validator: val => val === this.formData.password,
                    message: '密码与确认密码不同'
                }],
            },
        };
    },

    methods: {
        register({ validateResult, firstError }) {
            if (validateResult === true) {
                localStorage.setItem('token', 'register')
                axios.post(this.GLOBAL.SERVER + '/register', {
                    username: this.formData.username,
                    password: this.formData.password
                }).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            this.$message.success('注册成功');
                            setTimeout(() => {
                                this.$router.replace('/login');
                            }, 1);
                        } else {
                            if (res.data.msg === 'registration not enabled') {
                                this.$message.warning('已关闭注册')
                            } else if (res.data.msg === 'account name already taken') {
                                this.$message.warning('用户名已被占用');
                            } else {
                                this.$message.warning('注册失败');
                            }
                            
                        }
                    }
                    
                }).catch(error => {
                    this.$message.error('服务器错误');
                }).finally(() => {
                    localStorage.removeItem('token');
                })
            } else {
                this.$message.warning(firstError);
            }
        },
        reset() {
            this.formData = {
                username: '',
                password: '',
                confirmPassword: '',
            };
        },
    },
};
</script>

<style lang="less" scoped>
.register-container {
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

#submit {
    margin-left: 15px;
}

.t-form {
    width: 80%;
    margin-top: 20px;
    margin-bottom: 20px;
}
</style>