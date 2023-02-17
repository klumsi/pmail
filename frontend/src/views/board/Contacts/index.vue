<template>
    <div class="contacts-container">
        <ContentTitle title="通讯录" :count="this.pagination.total"></ContentTitle>
        <div class="menu-bar-container">
            <div class="menu-bar">
                <t-button variant="outline" @click="sendMail">发邮件</t-button>
                <template>
                    <t-drawer :visible="visAddContact" @close="visAddContact = false" :onConfirm="addContact"
                        header="添加联系人">
                        <div class="t-drawer-demo-div">
                            <p>昵称</p>
                            <t-input placeholder="联系人昵称" v-model.trim="addForm.nickname" />
                        </div>
                        <div class="t-drawer-demo-div">
                            <p>邮箱地址</p>
                            <t-input placeholder="联系人邮箱地址" v-model.trim="addForm.address" />
                        </div>
                    </t-drawer>
                    <t-button variant="outline" @click="visAddContact = true">
                        <t-icon name="add"></t-icon>
                        添加联系人
                    </t-button>
                </template>
                <t-popconfirm theme="warning" content="确认删除选中的联系人？该操作无法撤销" :onConfirm="delContact" v-model="visDelConfirm"
                    :visible="visDelConfirm">
                    <t-button variant="outline">
                        <t-icon name="remove"></t-icon>
                        删除联系人
                    </t-button>
                </t-popconfirm>
            </div>
        </div>

        <t-table class="tdesign-demo__select-single" row-key="id" :columns="columns" :data="data" table-layout="fixed"
            :hover="true" :pagination="pagination" :selectedRowKeys="selectedRowKeys" @select-change="selectChange"
            :loading="loading">
            <template #empty>
                <span>无联系人</span>
            </template>
        </t-table>

    </div>
</template>

<script>
import axios from 'axios';
import bus from '@/EventBus'
import ContentTitle from '@/components/ContentTitle'

export default {
    components: {
        ContentTitle
    },
    data() {
        return {
            columns: [
                { colKey: 'select', type: 'multiple', width: 40 },
                { colKey: 'nickname', title: '昵称', width: 160, ellipsis: true },
                { colKey: 'address', title: '邮箱地址', width: 420, ellipsis: true },
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
            addForm: {
                nickname: '',
                address: '',
            },
            visAddContact: false,
            visDelConfirm: false,
            search: '',
        }
    },
    methods: {
        sendMail() {
            let contactSelected = []
            const selected = Array.from(this.selectedRowKeys);
            this.data.forEach(item => {
                if (selected.includes(item.id)) {
                    contactSelected.push(item.address);
                }
            });
            bus.$emit('contactSelected', contactSelected);
            this.selectedRowKeys = [];
            this.$router.push('/compose')
        },
        selectChange(value, { selectedRowData }) {
            this.selectedRowKeys = value;
        },
        addContact() {
            if (this.addForm.nickname.length === 0) {
                this.$message.warning('昵称为空');
                return;
            }
            if (this.addForm.nickname.length > 30) {
                this.$message.warning('昵称超长');
                return;
            }
            if (this.addForm.address.length === 0) {
                this.$message.warning('邮箱地址为空');
                return;
            }
            if (!/^[A-Za-z0-9]+([_\.][A-Za-z0-9]+)*@([A-Za-z0-9\-]+\.)+[A-Za-z]{2,6}$/.test(this.addForm.address)) {
                this.$message.warning('邮箱格式错误');
                return;
            }
            const contact = {
                creator: this.getUsername(),
                nickname: this.addForm.nickname,
                address: this.addForm.address
            }
            axios.post(this.GLOBAL.SERVER + 'contact' + '/' + this.getUsername(), contact).then(res => {
                if (res.data.success) {
                    this.$message.success('添加成功');
                    this.visAddContact = false;
                    this.refresh();
                    this.selectedRowKeys = [];
                    this.addForm = [];
                } else {
                    this.$message.warning('联系人已存在');
                }
            }).catch(error => {
                this.$message.error('服务器错误');
            })

        },
        delContact() {
            if (this.selectedRowKeys.length === 0) {
                this.$message.warning('未选择任何邮件');
                return;
            }
            axios.delete(this.GLOBAL.SERVER + 'contact/' + this.getUsername(), {
                data: {
                    ids: this.selectedRowKeys
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
        },
        refresh() {
            axios.get(this.GLOBAL.SERVER + 'contact' + '/' + this.getUsername()).then(res => {
                if (res.data.success) {
                    this.data = res.data.data;
                    this.pagination.total = this.data.length;
                    this.loading = false;
                } else {
                    this.$message.warning('刷新失败');
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
        if (this.GLOBAL.init.isInit === false) {
            this.GLOBAL.init.originFolder = this.getFolder();
            this.$router.replace('/compose');
            return;
        }
        axios.get(this.GLOBAL.SERVER + 'contact' + '/' + this.getUsername()).then(res => {
            if (res.data.success) {
                this.data = res.data.data;
                this.pagination.total = this.data.length;
                this.loading = false;
                this.allData = this.data;
            } else {
                this.$message.warning('获取联系人失败');
            }
        }).catch(error => {
            this.$message.error('服务器错误');
        })
    },
    watch: {
        search: {
            handler(newVal, oldVal) {
                this.data = this.allData.filter(data => data.nickname.includes(newVal) || data.address.includes(newVal));
                this.pagination.total = this.data.length;
            },
        }
    }

}
</script>

<style lang="less" scoped>
.contacts-container {
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


/deep/ .t-button--variant-text {
    height: 44px;
}

/deep/ .t-drawer__content-wrapper {
    text-align: left;
}
</style>