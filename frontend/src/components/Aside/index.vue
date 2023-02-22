<template>
    <div class="aside-container">
        <t-menu :defaultValue="active">
            <t-menu-item value="/compose" to="/compose">
                <Edit1Icon/>
                &nbsp;写邮件
            </t-menu-item>
            <t-menu-item value="/contacts" to="/contacts">
                <UsergroupIcon/>
                &nbsp;通讯录
            </t-menu-item>
            <t-menu-item value="/inbox" to="/inbox">
                <MailIcon/>
                <span>
                    &nbsp;收件箱&nbsp;
                    <strong>
                        {{ inboxUnread > 0 ? inboxUnread : '' }}
                    </strong>
                </span>
                
            </t-menu-item>
            <t-menu-item value="/sent" to="/sent">
                <CheckCircleIcon/>
                &nbsp;已发送&nbsp;
                <strong>
                    {{ sentUnread > 0 ? sentUnread : '' }}
                </strong>
            </t-menu-item>
            <t-menu-item value="/drafts" to="/drafts">
                <RootListIcon/>
                &nbsp;草稿箱
            </t-menu-item>
            <t-menu-item value="/archive" to="/archive">
                <ControlPlatformIcon/>
                &nbsp;归档&nbsp;
                <strong>
                    {{ archiveUnread > 0 ? archiveUnread : '' }}
                </strong>
            </t-menu-item>
            <t-menu-item value="/trash" to="/trash">
                <DeleteIcon/>
                &nbsp;已删除&nbsp;
                <strong>
                    {{ trashUnread > 0 ? trashUnread : '' }}
                </strong>
            </t-menu-item>
            <t-menu-item value="/junk" to="/junk">
                <FilterClearIcon/>
                &nbsp;垃圾邮件&nbsp;
                <strong>
                    {{ junkUnread > 0 ? junkUnread : '' }}
                </strong>
            </t-menu-item>
        </t-menu>
    </div>
</template>

<script>
import { Edit1Icon, UsergroupIcon, MailIcon, CheckCircleIcon, RootListIcon, ControlPlatformIcon, DeleteIcon, FilterClearIcon } from 'tdesign-icons-vue';
import bus from '@/EventBus';
import axios from 'axios';

export default {
    props: ['active'],
    components: {
        Edit1Icon,
        UsergroupIcon,
        MailIcon,
        CheckCircleIcon,
        RootListIcon,
        ControlPlatformIcon,
        DeleteIcon,
        FilterClearIcon,
    },
    data() {
        return {
            inboxUnread: 0,
            sentUnread: 0,
            archiveUnread: 0,
            trashUnread: 0,
            junkUnread: 0
        }
    },
    methods: {
        getUnread(folder) {
            axios.get(this.GLOBAL.SERVER + '/mail/' + this.getUsername() + '/' + folder).then(res => {
                if (res.data.msg === 'authentication failed') {
                    this.$message.error('认证失败 请重新登录');
                    localStorage.removeItem('token');
                    setTimeout(() => {
                        this.$router.replace('/login');
                    }, 1);
                } else {
                    if (res.data.success) {
                        let sum = 0;
                        res.data.data.forEach(r => {
                            if (r.status === 0) {
                                sum++;
                            }
                        })
                        if (folder === 'inbox') {
                            this.inboxUnread = sum;
                        } else if (folder === 'sent') {
                            this.sentUnread = sum;
                        } else if (folder === 'archive') {
                            this.archiveUnread = sum;
                        } else if (folder === 'trash') {
                            this.trashUnread = sum;
                        } else if (folder === 'junk') {
                            this.junkUnread = sum;
                        }
                    }
                }
            })
        }
    },
    created() {
        bus.$on('inboxUnread', val => {
            this.inboxUnread = val;
        })
        bus.$on('sentUnread', val => {
            this.sentUnread = val;
        })
        bus.$on('archiveUnread', val => {
            this.archiveUnread = val;
        })
        bus.$on('trashUnread', val => {
            this.trashUnread = val;
        })
        bus.$on('junkUnread', val => {
            this.junkUnread = val;
        })
        
        let boxList = ['inbox', 'sent', 'archive', 'trash', 'junk'];
        boxList.forEach(e => {
            this.getUnread(e)
        })
    }
}
</script>

<style lang="less" scoped>
.aside-container {
    height: 100%;
}

.t-default-menu {
    border: 6px solid #f0f0f0;
}
</style>