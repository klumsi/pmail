<template>
    <div class="header-operation">
        <div v-if="isLoggedIn" class="dropdown-box">
            <t-dropdown trigger="click" placement="bottom-right" @click="clickHandler">
                <t-button variant="text">
                    <t-avatar>{{ userInfo.username.substr(0, 2) }}</t-avatar>
                    &nbsp; {{ userInfo.username + '@' + this.GLOBAL.DOMAIN }}
                    <ChevronDownIcon size="20"/>
                </t-button>
                <t-dropdown-menu>
                    <t-dropdown-item :value="1">
                        <EditIcon/>
                        &nbsp;修改昵称
                    </t-dropdown-item>
                    <t-dropdown-item :value="2">
                        <LockOnIcon/>
                        &nbsp;修改密码
                    </t-dropdown-item>
                    <t-dropdown-item :value="3">
                        <SettingIcon/>
                        &nbsp;帐号管理
                    </t-dropdown-item>
                    <t-dropdown-item :value="4">
                        <PoweroffIcon/>
                        &nbsp;退出登录
                    </t-dropdown-item>
                </t-dropdown-menu>
            </t-dropdown>
        </div>

        <div v-else class="button-box">
            <t-button variant="text" @click="toRegister">注册</t-button>
            <t-button variant="text" @click="toLogin">登录</t-button>
        </div>

        <t-drawer :visible="visChangeNickname" @close="visChangeNickname = false" :onConfirm="changeNickname" header="修改昵称"
            confirmBtn="修改">
            <div class="t-drawer-demo-div">
                <p>发件人昵称</p>
                <t-input placeholder="输入发件人昵称" v-model="userInfo.nickname"></t-input>
            </div>
        </t-drawer>

        <t-drawer :visible="visChangePassword" @close="visChangePassword = false" :onConfirm="changePassword" header="修改密码"
            confirmBtn="修改">
            <div class="t-drawer-demo-div">
                <p>当前密码</p>
                <t-popup content="6~16个字符" destroyOnClose trigger="focus">
                    <t-input type="password" placeholder="输入当前密码" v-model.trim="changePasswordForm.password"></t-input>
                </t-popup>

                <p>新密码</p>
                <t-popup content="6~16个字符" destroyOnClose trigger="focus">
                    <t-input type="password" placeholder="输入新密码" v-model.trim="changePasswordForm.newPassword"></t-input>
                </t-popup>

                <p>确认新密码</p>
                <t-popup content="6~16个字符" destroyOnClose trigger="focus">
                    <t-input type="password" placeholder="再次输入新密码"
                        v-model.trim="changePasswordForm.confirmNewPassword"></t-input>
                </t-popup>

            </div>
        </t-drawer>

        <t-drawer :visible="visAccountManagement" @close="visAccountManagement = false" header="帐号管理" :footer="false">
            <div class="t-drawer-demo-div">
                <t-popconfirm theme="danger" content="是否删除所有帐号及全部数据？该操作无法撤销" v-model="visDeleteAlert"
                    :onConfirm="deleteAccount">
                    <t-button theme="danger">删除账号</t-button>
                </t-popconfirm>
            </div>
        </t-drawer>


    </div>
</template>

<script>
import axios from 'axios'
import { ChevronDownIcon, EditIcon, LockOnIcon, SettingIcon, PoweroffIcon } from 'tdesign-icons-vue';

export default {
    name: 'Dropdown',
    props: ['isLoggedIn', 'userInfo'],
    data() {
        return {
            visChangeNickname: false,
            visChangePassword: false,
            visAccountManagement: false,
            visDeleteAlert: false,
            changePasswordForm: {
                password: '',
                newPassword: '',
                confirmNewPassword: ''
            }
        }
    },
    components: {
        ChevronDownIcon, 
        EditIcon, 
        LockOnIcon, 
        SettingIcon, 
        PoweroffIcon
    },
    methods: {
        clickHandler(data) {
            if (data.value === 1) {
                this.visChangeNickname = true;
            } else if (data.value === 2) {
                this.visChangePassword = true
            } else if (data.value === 3) {
                this.visAccountManagement = true
            } else if (data.value === 4) {
                localStorage.removeItem('token');
                this.$message.success('已退出');
                setTimeout(() => {
                    this.$router.replace('/login');
                }, 1);
            }
        },
        toRegister() {
            const path = this.$route.path
            if (path != '/register') {
                this.$router.replace('/register')
            }
        },
        toLogin() {
            const path = this.$route.path
            if (path != '/login') {
                this.$router.replace('/login')
            }
        },
        changeNickname() {
            if (this.userInfo.nickname.trim().length === 0) {
                this.$message.warning('昵称为空');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/user/' + this.userInfo.username, {
                user: {
                    username: this.userInfo.username,
                    nickname: this.userInfo.nickname
                }
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success("修改成功");
                        this.visChangeNickname = false;
                    } else {
                        this.$message.warning('修改失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error("服务器错误");
            })
        },
        changePassword() {
            const patt = /^[^]{6,16}$/;
            if (this.changePasswordForm.password === "") {
                this.$message.warning("当前密码为空");
            } else if (this.changePasswordForm.newPassword === "") {
                this.$message.warning("新密码为空");
            } else if (!patt.test(this.changePasswordForm.newPassword)) {
                this.$message.warning("新密码格式错误");
            } else if (this.changePasswordForm.confirmNewPassword === "") {
                this.$message.warning("确认新密码为空");
            } else if (!patt.test(this.changePasswordForm.confirmNewPassword)) {
                this.$message.warning("确认新密码格式错误");
            } else if (this.changePasswordForm.newPassword !== this.changePasswordForm.confirmNewPassword) {
                this.$message.warning("新密码与确认密码不一致");
            } else {
                axios.put(this.GLOBAL.SERVER + '/user/' + this.userInfo.username, {
                    user: {
                        username: this.userInfo.username,
                        password: this.changePasswordForm.password
                    },
                    newPassword: this.changePasswordForm.newPassword
                }).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            this.$message.success("修改成功");
                            localStorage.removeItem('token');
                            setTimeout(() => {
                                this.visChangePassword = false;
                                this.$router.go(0);
                            }, 1000);
                        } else {
                            this.$message.warning("密码错误");
                        }
                    }
                    
                }).catch(error => {
                    this.$message.error("服务器错误");
                })
            }
        },
        deleteAccount() {
            axios.delete(this.GLOBAL.SERVER + '/user/' + this.userInfo.username).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success("已删除");
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.visChangePassword = false;
                            this.$router.go(0);
                        }, 1000);
                    } else {
                        this.$message.warning("删除失败");
                    }
                }
                
            }).catch(res => {
                this.$message.error("服务器错误");
            })
        }
    },
}
</script>

<style lang="less" scoped>
/deep/ .t-button--variant-text {
    height: 44px;
}

/deep/ .t-drawer__content-wrapper {
    text-align: left;
}

#delBtn {
    margin-top: 30px;
}
</style>