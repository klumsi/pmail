<template>
    <div class="box-container">
        <ContentTitle :title="title" :count="this.pagination.total"></ContentTitle>
        <div class="menu-bar-container">
            <div class="menu-bar">
                <t-button variant="outline" @click="refresh()">
                    <RefreshIcon/>
                    刷新
                </t-button>
                <t-button v-if="getFolder() != 'drafts'" variant="outline" @click="markAsRead">标为已读</t-button>
                <t-button v-if="getFolder() != 'drafts'" variant="outline" @click="markAsUnread">标为未读</t-button>
                <t-button v-if="getFolder() != 'trash' && getFolder() != 'junk' && getFolder() != 'drafts'" variant="outline" @click="deleteMail($event, false)">删除</t-button>
                <t-popconfirm theme="warning" content="是否删除所选邮件？该操作无法撤销" v-model="visDeleteAlert" :onConfirm="deleteMail">
                    <t-button variant="outline">彻底删除</t-button>
                </t-popconfirm>
                <t-dropdown v-if="getFolder() != 'drafts' && getFolder() != 'junk'" :options="options" @click="clickHandler">
                    <t-button variant="outline">
                        移动到
                        <ChevronDownIcon/>
                    </t-button>
                </t-dropdown>
                <t-button v-if="getFolder() != 'junk' && getFolder() != 'sent' && getFolder() != 'drafts'" variant="outline" @click="markAsJunk">标记为垃圾邮件</t-button>
                <t-button v-if="getFolder() == 'junk'" variant="outline" @click="markAsUnjunk">标记为非垃圾邮件</t-button>
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
                                <svg t="1670745502797" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2754" width="16" height="16"><path d="M508.4 575.1l400.8-383.4c-1.6-0.1-3.1-0.2-4.7-0.2H121.2c-4.3 0-8.4 0.5-12.4 1.4l399.6 382.2z" fill="#333333" p-id="2755"></path><path d="M958.6 233.2l-0.9 0.6-413.3 395.4c-10 9.5-23 14.3-36 14.3s-26.1-4.8-36-14.3l-406.7-389c-0.3 2.4-0.5 4.8-0.5 7.3v486c0 54 44 98 98 98h699.3c54 0 98-44 98-98v-486c-0.3-15.1-1.9-14.3-1.9-14.3z" fill="#333333" p-id="2756"></path></svg>
                            );
                        } else if (row.status === 1) {
                            return (
                                <svg t="1670746383467" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5076" width="16" height="16"><path d="M170.666667 341.333333l341.333333 213.333334 341.333333-213.333334-341.333333-213.333333-341.333333 213.333333m768 0v426.666667a85.333333 85.333333 0 0 1-85.333334 85.333333H170.666667a85.333333 85.333333 0 0 1-85.333334-85.333333V341.333333c0-31.146667 16.64-58.026667 41.386667-72.96L512 27.306667l385.28 241.066666c24.746667 14.933333 41.386667 41.813333 41.386667 72.96z" fill="#333333" p-id="5077"></path></svg>
                            );
                        }
                    }
                },
                { colKey: 'fromName', title: '发件人', width: 160, ellipsis: true },
                { colKey: 'subject', title: '主题', width: 420,  ellipsis: true },
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
                    if (res.data.success) {
                        this.$router.push('/compose');
                        setTimeout(() => {
                            bus.$emit('draftForm', {
                                subject: res.data.data.subject,
                                address: res.data.data.address,
                                content: res.data.data.content
                            });
                        }, 100);
                    } else {
                        this.$message.warning('获取草稿失败');
                    }
                }).catch(error => {
                    this.$message.error('服务器错误');
                })
                return;
            }
            if (e.colIndex > 0) {
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
                if (res.data.success) {
                    this.$message.success('移动成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('移动失败');
                }
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        },
        refresh() {
            this.loading = true;
            const that = this;
            const currentPage = this.pagination.current;
            axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder()).then(res => {
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
                            d.fromName += ' ' + d.fromAddress;
                        });
                        this.pagination.total = res.data.data.length;
                        this.pagination.defaultCurrent = currentPage;
                    } else {
                        this.data = [];
                    }
                    this.pagination.total = this.data.length;
                } else {
                    this.$message.warning("获取邮件列表失败");
                }
            }).catch(error => {
                this.$message.error("服务器错误");
            }).finally(() => {
                that.loading = false;
            })
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
                if (res.data.success) {
                    this.$message.success('标记成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('标记失败');
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
                if (res.data.success) {
                    this.$message.success('标记成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('标记失败');
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
                if (res.data.success) {
                    this.$message.success('标记成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('标记失败');
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
                if (res.data.success) {
                    this.$message.success('标记成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('标记失败');
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
                if (res.data.success) {
                    this.$message.success('删除成功');
                    this.refresh();
                    this.selectedRowKeys = []
                } else {
                    this.$message.warning('删除失败');
                }
            }).catch(error => {
                this.$message.error('服务器错误');
            })
        }
    },
    beforeCreate() {
        bus.$on('search', val => {
            this.search = val;
        });

        axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + this.getFolder()).then(res => {
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
                        d.fromName += ' ' + d.fromAddress;
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
        }).catch(error => {
            this.$message.error("服务器错误");
        })
    },
    created() {

        if (this.getFolder() === 'drafts') {
            this.columns = [
                { colKey: 'select', type: 'multiple', width: 40 },
                { colKey: 'subject', title: '主题', width: 420,  ellipsis: true },
                { colKey: 'date', title: '时间', width: 170 },
            ]
        } else if (this.getFolder() === 'sent') {
            this.columns = [
                { colKey: 'select', type: 'multiple', width: 40 },
                { colKey: 'subject', title: '主题', width: 420,  ellipsis: true },
                { colKey: 'date', title: '时间', width: 170 },
            ]
        }
        // setInterval(() => {
        //     this.refresh();
        // }, 60000);
    },
    watch: {
        search: {
            handler(newVal, oldVal) {
                this.data = this.allData.filter(data => data.subject.includes(newVal) || data.fromName.includes(newVal) || data.fromAddress.includes(newVal));
                this.pagination.total = this.data.length;
            },
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