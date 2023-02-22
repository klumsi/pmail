<template>
    <div class="box-container">
        <ContentTitle :title="title" :count="this.pagination.total"></ContentTitle>
        <div class="menu-bar-container">
            <div class="menu-bar">
                <t-button variant="outline" @click="refresh()">
                    <RefreshIcon />
                    刷新
                </t-button>
                <t-button v-if="getFolder() != 'drafts'" variant="outline" @click="markAsRead">标为已读</t-button>
                <t-button v-if="getFolder() != 'trash' && getFolder() != 'junk' && getFolder() != 'drafts'"
                    variant="outline" @click="deleteMail($event, false)">删除</t-button>
                <t-popconfirm theme="warning" content="是否删除所选邮件？该操作无法撤销" v-model="visDeleteAlert" :onConfirm="deleteMail">
                    <t-button variant="outline">彻底删除</t-button>
                </t-popconfirm>
                <t-dropdown v-if="getFolder() != 'drafts'" :options="mark_options" @click="markHandler">
                    <t-button variant="outline">
                        标记为
                        <ChevronDownIcon />
                    </t-button>
                </t-dropdown>
                <t-dropdown v-if="getFolder() != 'drafts' && getFolder() != 'junk'" :options="options"
                    @click="clickHandler">
                    <t-button variant="outline">
                        移至
                        <ChevronDownIcon />
                    </t-button>
                </t-dropdown>
            </div>
        </div>
        <t-table class="tdesign-demo__select-single" row-key="id" :columns="columns" :data="data" table-layout="fixed"
            :hover="true" :pagination="pagination" :onCellClick="onCellClick" :selectedRowKeys="selectedRowKeys"
            @select-change="selectChange" :loading="loading" @page-change="onPageChange">
            <template #empty>
                <span>暂无邮件</span>
            </template>
        </t-table>
    </div>
</template>

<script>
import axios from 'axios';
import bus from '@/EventBus';
import ContentTitle from '@/components/ContentTitle';
import { RefreshIcon, ChevronDownIcon } from 'tdesign-icons-vue';

export default {
    components: {
        ContentTitle,
        RefreshIcon,
        ChevronDownIcon,
    },
    props: ['title'],
    data() {
        return {
            columns: [
                { colKey: 'select', type: 'multiple', width: 40 },
                {
                    colKey: 'status',
                    width: 40,
                    cell: (h, { row }) => {
                        if (row.status === 0) {
                            return (
                                <svg style="margin-top:5px" t="1676970469352" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4021" width="16" height="16"><path d="M508.4 575.1l400.8-383.4c-1.6-0.1-3.1-0.2-4.7-0.2H121.2c-4.3 0-8.4 0.5-12.4 1.4l399.6 382.2z" fill="#efb336" p-id="4022"></path><path d="M958.6 233.2l-0.9 0.6-413.3 395.4c-10 9.5-23 14.3-36 14.3s-26.1-4.8-36-14.3l-406.7-389c-0.3 2.4-0.5 4.8-0.5 7.3v486c0 54 44 98 98 98h699.3c54 0 98-44 98-98v-486c-0.3-15.1-1.9-14.3-1.9-14.3z" fill="#efb336" p-id="4023"></path></svg>
                            );
                        } else if (row.status === 1) {
                            return (
                                <svg style="margin-top:5px" t="1676970547431" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5257" width="16" height="16"><path d="M947.8 439.8L541.6 757.2c-16.3 12.8-42.8 12.8-59.1 0L76.2 439.8c-16.3-12.8-16.3-33.4 0-46.2L482.4 76.3c16.3-12.8 42.8-12.8 59.1 0l406.2 317.3c16.4 12.8 16.4 33.5 0.1 46.2z" fill="#bfbfbf" p-id="5258"></path><path d="M581 807.6c-19.1 14.9-43.6 23.1-69 23.1s-49.9-8.2-69-23.1L64 511.5V827c0 55.2 44.8 100 100 100h696c55.2 0 100-44.8 100-100V511.5L581 807.6z" fill="#707070" p-id="5259"></path></svg>
                            );
                        }
                    }
                },
                { colKey: 'name', title: '发件人', width: 160, ellipsis: true },
                { colKey: 'subject', title: '主题', width: 420, ellipsis: true },
                { colKey: 'date', title: '时间', width: 170 },
            ],
            data: [],
            allData: [],
            pagination: {
                current: 1,
                pageSize: 10,
                defaultCurrent: 1,
                defaultPageSize: 10,
                total: 0,
                showJumper: true,
                pageSizeOptions: [],
                loading: true
            },
            selectedRowKeys: [],
            loading: true,
            options: [
                { content: '收件箱', value: 'inbox' },
                { content: '已发送', value: 'sent' },
                { content: '归档', value: 'archive' },
            ],
            mark_options: [
                { content: '未读邮件', value: 'unread' },
                { content: '垃圾邮件', value: 'junk' },
            ],
            visDeleteAlert: false,
            search: ''
        }
    },
    methods: {
        onCellClick(e) {
            if (this.getFolder() === 'drafts') {
                if (e.colIndex === 0) {
                    return;
                }
                axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder() + '/' + e.row.id).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            this.$router.push('/compose');
                            setTimeout(() => {
                                bus.$emit('draftForm', {
                                    subject: res.data.data.subject,
                                    address: res.data.data.address,
                                    content: res.data.data.content
                                });
                            }, 1);
                        } else {
                            this.$message.warning('获取草稿失败');
                        }
                    }

                }).catch(error => {
                    this.$message.error('服务器错误');
                })
                return;
            }
            if (e.colIndex > 0) {
                this.refresh();
                this.$router.push('/' + this.getFolder() + '/' + e.row.id);
            }
        },
        selectChange(value, { selectedRowData }) {
            this.selectedRowKeys = value;
        },
        clickHandler(data) {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            if (data.value === this.getFolder()) {
                this.$message.warning('选择的文件夹与当前文件夹相同');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MOVE',
                ids: this.selectedRowKeys,
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
                        this.refresh();
                        this.selectedRowKeys = []
                    } else {
                        this.$message.warning('移动失败');
                    }
                }

            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        markHandler(data) {
            if (data.value === 'unread') {
                this.markAsUnread();
            } else if (data.value === 'junk') {
                this.markAsJunk();
            } else if (data.value === 'unjunk') {
                this.markAsUnjunk();
            }
        },
        refresh() {
            this.loading = true;
            const that = this;
            const currentPage = this.pagination.current;

            if (this.getFolder() == 'sent' || this.getFolder() == 'drafts') {
                axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/full/' + this.getFolder()).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            if (res.data.data) {
                                this.data = res.data.data.reverse();
                                this.data.sort((a, b) => {
                                    if (a.status != b.status) {
                                        return a.status - b.status;
                                    } else {
                                        return b.timestamp - a.timestamp;
                                    }
                                })
                                this.data.forEach(d => {
                                    d.name = d.recipients.map((e, i) => {
                                        return e.name;
                                    }).join(", ")
                                });
                                this.pagination.total = res.data.data.length;
                                this.allData = this.data;
                            } else {
                                this.data = [];
                                this.allData = this.data;
                            }
                            this.loading = false;
                        } else {
                            this.$message.warning("获取邮件列表失败");
                        }
                    }

                }).catch(error => {
                    this.$message.error("服务器错误");
                })
            } else {
                axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder()).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            if (res.data.data) {
                                this.data = res.data.data.reverse();
                                this.data.sort((a, b) => {
                                    if (a.status != b.status) {
                                        return a.status - b.status;
                                    } else {
                                        return b.timestamp - a.timestamp;
                                    }
                                })
                                this.data.forEach(d => {
                                    if (d.fromName.length === 0) {
                                        d.name = d.fromAddress.substring(1, d.fromAddress.indexOf('@'));
                                    } else {
                                        d.name = d.fromName;
                                    }
                                });
                                this.pagination.total = res.data.data.length;
                                this.allData = this.data;
                            } else {
                                this.data = [];
                                this.allData = this.data;
                            }
                            this.loading = false;
                        } else {
                            this.$message.warning("获取邮件列表失败");
                        }
                    }

                }).catch(error => {
                    this.$message.error("服务器错误");
                })
            }

        },
        onPageChange(pageInfo) {
            this.pagination.current = pageInfo.current;
        },
        markAsRead() {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MARK_AS_READ',
                ids: this.selectedRowKeys
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
        markAsUnread() {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MARK_AS_UNREAD',
                ids: this.selectedRowKeys
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
        markAsJunk() {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            axios.put(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                type: 'MOVE',
                ids: this.selectedRowKeys,
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
        deleteMail(e, permanent) {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            if (permanent === undefined) {
                permanent = true;
            }
            axios.delete(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder(), {
                data: {
                    type: "DELETE",
                    ids: this.selectedRowKeys,
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
                        this.refresh();
                        this.selectedRowKeys = []
                    } else {
                        this.$message.warning('删除失败');
                    }
                }

            }).catch(error => {
                this.$message.error('服务器错误');
            })
        }
    },
    created() {
        bus.$on('search', val => {
            this.search = val;
        });

        if (this.getFolder() === 'junk') {
            this.mark_options = [
                { content: '未读邮件', value: 'unread' },
                { content: '非垃圾邮件', value: 'unjunk' },
            ]
        }
        
        if (this.getFolder() === 'drafts') {
            this.columns = [
                { colKey: 'select', type: 'multiple', width: 40 },
                {
                    colKey: 'status',
                    width: 40,
                    cell: (h, { row }) => {
                        return (
                            <svg style="margin-top:5px" t="1676990724432" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="21694" width="16" height="16"><path d="M418.4 518.8L402.5 583c-5 20.2 16.3 41.7 36.6 37l65.1-15.2c4.1-0.9 7.6-2.9 10.5-5.7L953 160.8c9.6-9.6 8.2-26.7-3.3-38.1l-49.2-49.2c-11.4-11.4-28.5-12.9-38.1-3.3L424 508.6c-2.7 2.8-4.7 6.3-5.6 10.2z" fill="#707070" p-id="21695"></path><path d="M560 644.3a85.01 85.01 0 0 1-41.2 22.8l-65.1 15.2c-6.6 1.5-13.5 2.3-20.3 2.3-28.7 0-56.8-14-75.1-37.4-18.1-23.1-24.6-52.1-17.8-79.6l15.8-64.2c3.8-15.2 11.6-29.1 22.5-40.1L743.9 98.2H185.6c-48.4 0-87.6 39.2-87.6 87.6v654.9c0 48.4 39.2 87.6 87.6 87.6h654.9c48.4 0 87.6-39.2 87.6-87.6V276.3L560 644.3z" fill="#bfbfbf" p-id="21696"></path></svg>
                        )
                    }

                },
                { colKey: 'name', title: '收件人', width: 160, ellipsis: true },
                { colKey: 'subject', title: '主题', width: 420, ellipsis: true },
                { colKey: 'date', title: '时间', width: 170 },
            ];
            axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/full/' + this.getFolder()).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        if (res.data.data) {
                            this.data = res.data.data.reverse();
                            this.data.sort((a, b) => {
                                if (a.status != b.status) {
                                    return a.status - b.status;
                                } else {
                                    return b.timestamp - a.timestamp;
                                }
                            })
                            this.data.forEach(d => {
                                d.name = d.recipients.map((e, i) => {
                                    return e.name;
                                }).join(", ")
                            });
                            this.pagination.total = res.data.data.length;
                            this.allData = this.data;
                        } else {
                            this.data = [];
                            this.allData = this.data;
                        }
                        this.loading = false;
                    } else {
                        this.$message.warning("获取邮件列表失败");
                    }
                }

            }).catch(error => {
                this.$message.error("服务器错误");
            })
        } else if (this.getFolder() === 'sent') {
            this.columns = [
                { colKey: 'select', type: 'multiple', width: 40 },
                {
                    colKey: 'status',
                    width: 40,
                    cell: (h, { row }) => {
                        if (row.status === 0) {
                            return (
                                <svg style="margin-top:5px" t="1676970469352" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4021" width="16" height="16"><path d="M508.4 575.1l400.8-383.4c-1.6-0.1-3.1-0.2-4.7-0.2H121.2c-4.3 0-8.4 0.5-12.4 1.4l399.6 382.2z" fill="#efb336" p-id="4022"></path><path d="M958.6 233.2l-0.9 0.6-413.3 395.4c-10 9.5-23 14.3-36 14.3s-26.1-4.8-36-14.3l-406.7-389c-0.3 2.4-0.5 4.8-0.5 7.3v486c0 54 44 98 98 98h699.3c54 0 98-44 98-98v-486c-0.3-15.1-1.9-14.3-1.9-14.3z" fill="#efb336" p-id="4023"></path></svg>
                            );
                        } else if (row.status === 1) {
                            return (
                                <svg style="margin-top:5px" t="1676970547431" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5257" width="16" height="16"><path d="M947.8 439.8L541.6 757.2c-16.3 12.8-42.8 12.8-59.1 0L76.2 439.8c-16.3-12.8-16.3-33.4 0-46.2L482.4 76.3c16.3-12.8 42.8-12.8 59.1 0l406.2 317.3c16.4 12.8 16.4 33.5 0.1 46.2z" fill="#bfbfbf" p-id="5258"></path><path d="M581 807.6c-19.1 14.9-43.6 23.1-69 23.1s-49.9-8.2-69-23.1L64 511.5V827c0 55.2 44.8 100 100 100h696c55.2 0 100-44.8 100-100V511.5L581 807.6z" fill="#707070" p-id="5259"></path></svg>
                            );
                        }
                    }
                },
                { colKey: 'name', title: '收件人', width: 160, ellipsis: true },
                { colKey: 'subject', title: '主题', width: 420, ellipsis: true },
                { colKey: 'date', title: '时间', width: 170 },
            ];
            axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/full/' + this.getFolder()).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        if (res.data.data) {
                            this.data = res.data.data.reverse();
                            this.data.sort((a, b) => {
                                if (a.status != b.status) {
                                    return a.status - b.status;
                                } else {
                                    return b.timestamp - a.timestamp;
                                }
                            })
                            this.data.forEach(d => {
                                d.name = d.recipients.map((e, i) => {
                                    return e.name;
                                }).join(", ")
                            });
                            this.pagination.total = res.data.data.length;
                            this.allData = this.data;
                        } else {
                            this.data = [];
                            this.allData = this.data;
                        }
                        this.loading = false;
                    } else {
                        this.$message.warning("获取邮件列表失败");
                    }
                }

            }).catch(error => {
                this.$message.error("服务器错误");
            })
        } else {
            axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder()).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        if (res.data.data) {
                            this.data = res.data.data.reverse();
                            this.data.sort((a, b) => {
                                if (a.status != b.status) {
                                    return a.status - b.status;
                                } else {
                                    return b.timestamp - a.timestamp;
                                }
                            })
                            this.data.forEach(d => {
                                if (d.fromName.length === 0) {
                                    d.name = d.fromAddress.substring(1, d.fromAddress.indexOf('@'));
                                } else {
                                    d.name = d.fromName;
                                }
                            });
                            this.pagination.total = res.data.data.length;
                            this.allData = this.data;
                        } else {
                            this.data = [];
                            this.allData = this.data;
                        }
                        this.loading = false;
                    } else {
                        this.$message.warning("获取邮件列表失败");
                    }
                }

            }).catch(error => {
                this.$message.error("服务器错误");
            })
        }
    },
    watch: {
        search: {
            handler(newVal, oldVal) {
                this.data = this.allData.filter(data => data.name.includes(newVal) || data.subject.includes(newVal));
                this.pagination.total = this.data.length;
            },
        },
        allData: {
            handler(newVal, oldVal) {
                let sum = 0;
                newVal.forEach(msg => {
                    if (msg.status === 0) {
                        sum++;
                    }
                })

                let e = this.getFolder() + 'Unread';
                bus.$emit(e, sum);
                
            }
        }
    }
}
</script>

<style lang="less" scoped>
.box-container {
    margin-left: 30px;
    margin-right: 30px;
    margin-top: 5px;
    margin-bottom: 5px;
}

/deep/ .tdesign-demo__select-single {
    .t-table__row--selected {
        background-color: #f3f3f3;
    }

}

/deep/ .t-table td {
    padding: 11px 24px 9px 24px;
}

/deep/ .t-table th {
    padding: 11px 24px 9px 24px;
}

/deep/ .t-table td.t-table__cell-check {
    padding: 0;
}

/deep/ .t-table th.t-table__cell-check {
    padding: 0;
}

/deep/ .t-table__body {
    cursor: pointer;
    -webkit-text-size-adjust: none;
}

/deep/ .t-pagination__total {
    display: none;
}

.menu-bar-container {
    min-width: 670px;
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

}
</style>