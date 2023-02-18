<template>
    <div class="mail-container">
        <div class="menu-bar-container">
            <div class="menu-bar">
                <t-button variant="outline" @click="goBack">
                    <ChevronLeftIcon/>
                    返回
                </t-button>
                <t-button v-if="getFolder() != 'drafts'" variant="outline" @click="markAsRead">标为已读</t-button>
                <t-button v-if="getFolder() != 'drafts'" variant="outline" @click="markAsUnread">标为未读</t-button>
                <t-button v-if="getFolder() != 'trash' && getFolder() != 'junk' && getFolder() != 'drafts'" variant="outline" @click="deleteMail($event, false)">删除</t-button>
                <t-popconfirm theme="warning" content="是否删除所选邮件？该操作无法撤销" v-model="visDeleteAlert" :onConfirm="deleteMail">
                    <t-button variant="outline">彻底删除</t-button>
                </t-popconfirm>
                <t-dropdown v-if="getFolder() != 'drafts' && getFolder() != 'junk'" :options="options" @click="clickMoveHandler">
                    <t-button variant="outline">
                        移动到
                        <ChevronDownIcon/>
                    </t-button>
                </t-dropdown>
                <t-button v-if="getFolder() != 'junk' && getFolder() != 'sent' && getFolder() != 'drafts'" variant="outline" @click="markAsJunk">标记为垃圾邮件</t-button>
                <t-button v-if="getFolder() == 'junk'" variant="outline" @click="markAsUnjunk">标记为非垃圾邮件</t-button>

                <t-dropdown v-if="attachments.length > 0" :options="attachments" @click="clickAttachmentsHandler" maxColumnWidth="1000">
                    <t-button variant="outline" theme="primary">
                        <DownloadIcon/>
                        获取附件
                        <ChevronDownIcon/>
                    </t-button>
                </t-dropdown>
                    
                
            </div>
        </div>
        <div class="detail-container">
            <t-loading size="small" :loading="loading" showOverlay>

                <div class="mail-header">
                    <div class="mail-subject">{{ mail.subject }}</div>
                    <div class="mail-header-detail">

                        <span class="mail-header-detail-dim">
                            发件人：&nbsp;
                        </span>

                        <span class="mail-header-detail-bold">
                            {{ mail.fromName }}&nbsp;
                            {{ mail.fromAddress }}
                        </span>
                        
                        <span class="mail-header-detail-space"></span>
                        
                        <span class="mail-header-detail-dim">
                            {{ mail.date }}
                        </span>
                    </div>
                </div>
                
                <t-divider></t-divider>

                <div class="mail-content" v-html="mail.content"></div> 
            </t-loading>
        </div>
        
        
    </div>
</template>

<script>
import axios from 'axios';
import { ChevronDownIcon, ChevronLeftIcon, DownloadIcon } from 'tdesign-icons-vue';

export default {
    props: ['id'],
    data() {
        return {
            mail: '',
            options: [
                { content: '收件箱', value: 'inbox' },
                { content: '已发送', value: 'sent' },
                { content: '归档', value: 'archive' },
            ],
            attachments: [

            ],
            visDeleteAlert: false,
            loading: true
        }
    },
    components: {
        ChevronDownIcon,
        ChevronLeftIcon,
        DownloadIcon,
    },
    methods: {
        goBack() {
            this.$router.go(-1);
        },
        markAsRead() {
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MARK_AS_READ',
                ids: [this.id]
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('标记成功');
                    } else {
                        this.$message.warning('标记失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        markAsUnread() {
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MARK_AS_UNREAD',
                ids: [this.id]
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('标记成功');
                    } else {
                        this.$message.warning('标记失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        deleteMail(e, permanent) {
            if (permanent === undefined) {
                permanent = true;
            }
            axios.delete(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                data: {
                    type: "DELETE",
                    ids: [this.id],
                    deletePermanently: permanent
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
                        this.$message.success('删除成功');
                        this.$router.replace('/' + this.getFolder());
                    } else {
                        this.$message.warning('删除失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        clickMoveHandler(data) {
            if (data.value === this.getFolder()) {
                this.$message.warning('选择的文件夹与当前文件夹相同');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MOVE',
                ids: [this.id],
                destination: data.value
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('移动成功');
                        this.$router.replace('/' + this.getFolder());
                    } else {
                        this.$message.warning('移动失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        markAsJunk() {
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MOVE',
                ids: [this.id],
                destination: 'junk'
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('标记成功');
                        this.$router.replace('/' + this.getFolder());
                    } else {
                        this.$message.warning('标记失败');
                    }
                }
                
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        markAsUnjunk() {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MOVE',
                ids: this.selectedRowKeys,
                destination: 'inbox'
            }).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        this.$message.success('标记成功');
                        this.refresh();
                        this.selectedRowKeys = []
                    } else {
                        this.$message.warning('标记失败');
                    }
                }
 
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        clickAttachmentsHandler(data) {
            const url = this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder() + '/' + this.id + '/' + data.value;
            window.open(url, '_blank');
        }
    },
    created() {
        const that = this;
        axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder() + '/' + this.id).then(res => {
            if (res.data.msg === 'authentication failed') {
                this.$message.error('认证失败 请重新登录');
                localStorage.removeItem('token');
                setTimeout(() => {
                    this.$router.replace('/login');
                }, 1);
            } else {
                if (res.data.success) {
                    this.mail = res.data.data;
                    this.mail.attachments.forEach(item => {
                        const attachment = {
                            content: item,
                            value: item
                        };
                        
                        const temp = 'cid:' + item;
                        if (this.mail.content.search(temp) != -1) {
                            const forward = this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder() + '/' + this.id + '/' + item;
                            this.mail.content = this.mail.content.replace(temp, forward);
                        } else {
                            this.attachments.push(attachment);
                        }
                        
                    })
                    axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                        type: 'MARK_AS_READ',
                        ids: [this.id]
                    }).then(res => {
                            if (res.data.msg === 'authentication failed') {
                                this.$message.error('认证失败 请重新登录');
                                localStorage.removeItem('token');
                                setTimeout(() => {
                                    this.$router.replace('/login');
                                }, 1);
                            } else {
                                
                            }
                    })
                } else {
                    this.$message.warning('解析邮件失败');
                }
            }
            
        }).catch(error => {
            this.$message.error("服务器错误");
        }).finally(() => {
            that.loading = false;
        })
    }
}
</script>

<style lang="less" scoped>
.mail-container {
    margin-left: 30px;
    margin-right: 30px;
    margin-top: 20px;
    margin-bottom: 5px;
}

.menu-bar-container {
    min-width: 782px;
    background-color: #e3ecfa;
    border-radius: 5px;
    height: 44px;

    display: flex;
    flex-direction: row;
}


.menu-bar {
    .t-button {
        margin: 6px 2px 6px 2px;
        left: 6px;
    }
    display: flex;
}

.detail-container {
    background-color: white;
    margin-left: 28px;
    margin-right: 28px;
}

.mail-header {
    margin-top: 23px;
    .mail-subject {
    color: #2e3033;
    display: inline;
    font-size: 20px;
    font-weight: 700;
    letter-spacing: .45px;
    line-height: 24px;
    margin-right: 6px;
    user-select: text
}

    .mail-header-detail {
        min-width: 500px;
        color: #2e3033;
        display: flex;
        margin-top: 18px;
        .mail-header-detail-space {
            flex-grow: 1;
        }
        .mail-header-detail-bold {
            font-size: 13px;
            font-weight: 500;
        }

        .mail-header-detail-dim {
            font-size: 13px;
            color: #959da6;
        }
    }
}

.mail-content {
    margin-top: 30px;
    margin-bottom: 50px;
}


</style>
