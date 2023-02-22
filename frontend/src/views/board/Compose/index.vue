<template>
    <div class="compose-container">
        <ContentTitle title="新邮件"></ContentTitle>
        <div class="mail-info-item">
            <t-input-adornment prepend="发件人名称">
                <t-tag-input placeholder="输入地址" v-model="mailForm.address" clearable></t-tag-input>
            </t-input-adornment>
        </div>
        <div class="mail-info-item">
            <t-input-adornment prepend="主题">
                <t-input placeholder="输入主题" v-model="mailForm.subject" clearable></t-input>
            </t-input-adornment>
        </div>
        <div class="tool-bar">
            <t-upload :action="GLOBAL.SERVER + '/upload'" v-model="mailForm.attachments" theme="file-input"
                placeholder="未选择文件" @success="uploadSuccess"></t-upload>

        </div>
        <div class="text-area">
            <!-- <t-textarea v-model="value" placeholder="输入正文" :autosize="{ minRows: 18, maxRows: 18 }" :maxlength="5000" name="description"/> -->
            <ckeditor :editor="editor" v-model="mailForm.content" :config="editorConfig"></ckeditor>

        </div>
        <div class="no-sense"></div>
        <div class="func-bar">
            <t-input-adornment prepend="发件人名称">
                <t-input placeholder="输入发件人名称" v-model="userInfo.nickname" />
            </t-input-adornment>
            <div>
                <t-button theme="default" variant="outline" @click="saveDrafts" style="margin-right: 10px;"
                    :loading="saveLoading">存为草稿</t-button>
                <t-button @click="send" :loading="loading">发送</t-button>
            </div>
        </div>

    </div>
</template>

<script>
import bus from '@/EventBus'
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import ContentTitle from '@/components/ContentTitle';
import axios from 'axios';

export default {
    name: 'Compose',
    components: {
        ContentTitle
    },
    props: ['userInfo'],
    data() {
        return {
            mailForm: {
                username: this.userInfo.username,
                address: [],
                subject: '',
                nickname: this.userInfo.nickname,
                attachments: [],
                content: ''
            },
            loading: false,
            saveLoading: false,
            editor: ClassicEditor,
            editorData: null,
            editorConfig: {
                placeholder: '',
                toolbar: {
                    items: [
                        "heading",
                        "|",
                        "bold",
                        "italic",
                        "link",
                        "bulletedList",
                        "numberedList",
                        "|",
                        "outdent",
                        "indent",
                        "|",
                        "blockQuote",
                        "insertTable",
                        "undo",
                        "redo"
                    ]
                },
            }
        }
    },

    methods: {
        send() {
            if (this.check()) {
                this.loading = true;
                const that = this;
                axios.post(this.GLOBAL.SERVER + '/mail', {
                    username: this.userInfo.username,
                    address: this.mailForm.address,
                    subject: this.mailForm.subject,
                    nickname: this.userInfo.nickname,
                    originalAttachments: this.mailForm.attachments.length === 0 ? null : this.mailForm.attachments[0].name,
                    attachments: this.mailForm.attachments.length === 0 ? null : this.mailForm.attachments[0].response.data,
                    content: this.mailForm.content
                }).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            this.$message.success('已发送');
                            this.mailForm = {
                                username: this.userInfo.username,
                                address: [],
                                subject: '',
                                nickname: this.userInfo.nickname,
                                attachments: [],
                                content: ''
                            }
                            this.loading = false;
                        } else {
                            this.$message.warning('发送失败');
                        }
                    }
                    
                }).catch(error => {
                    this.$message.error('服务器错误');
                }).finally(() => {
                    that.loading = false;
                })
            }
        },
        check() {
            if (this.mailForm.address.length === 0) {
                this.$message.warning('收件人为空');
                return false;
            }
            const patt = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/
            const checkAddress = this.mailForm.address.every(item => patt.test(item))
            if (!checkAddress) {
                this.$message.warning('收件人格式错误');
                return false;
            }
            if (this.mailForm.subject.length === 0) {
                this.$message.warning('邮件主题为空');
                return false;
            }
            if (this.mailForm.content.length === 0) {
                this.$message.warning('邮件内容为空');
                return false;
            }
            if (this.userInfo.nickname.length === 0) {
                this.$message.warning('发件人名称为空');
                return false;
            }
            return true
        },
        uploadSuccess() {
            this.$message.success('上传成功');
        },
        saveDrafts() {
            if (this.check()) {
                this.saveLoading = true;
                const that = this;
                axios.post(this.GLOBAL.SERVER + '/mail/save', {
                    username: this.userInfo.username,
                    address: this.mailForm.address,
                    subject: this.mailForm.subject,
                    nickname: this.userInfo.nickname,
                    content: this.mailForm.content
                }).then(res => {
                    if (res.data.msg === 'authentication failed') {
                        this.$message.error('认证失败 请重新登录');
                        localStorage.removeItem('token');
                        setTimeout(() => {
                            this.$router.replace('/login');
                        }, 1);
                    } else {
                        if (res.data.success) {
                            this.$message.success('已保存');
                            this.mailForm = {
                                username: this.userInfo.username,
                                address: [],
                                subject: '',
                                nickname: this.userInfo.nickname,
                                attachments: [],
                                content: ''
                            }
                            this.saveLoading = false;
                            this.$router.push('/drafts')
                        } else {
                            this.$message.warning('保存失败');
                        }
                    }
                    
                }).catch(error => {
                    this.$message.error('服务器错误');
                }).finally(() => {
                    that.saveLoading = false;
                })
            }
        }
    },
    created() {
        bus.$on('contactSelected', val => {
            this.mailForm.address = val;
        })
        bus.$on('draftForm', val => {
            this.mailForm.subject = val.subject;
            this.mailForm.content = val.content;
            this.mailForm.address = val.address;
        })
        
    }
}
</script>

<style lang="less" scoped>
.compose-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;

    .mail-info-item {
        padding: 5px 30px;
    }

    .tool-bar {
        padding: 5px 30px;
        display: inline-flex;
        justify-content: space-between;
    }

    .text-area {
        padding: 5px 30px;
        padding-bottom: 30px;
        height: 50%;
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .func-bar {
        min-width: 432px;
        background-color: white;
        padding: 15px 30px;
        border-top: 1px solid var(--td-border-level-2-color);
        // border-top: 6px solid #f0f0f0;
        display: inline-flex;
        justify-content: space-between;
    }

    .no-sense {
        background-color: white;
    }
}

/deep/ .ck.ck-editor {
    height: calc(100% - 36px);
}

/deep/ .ck.ck-editor__main {
    height: 100%;
}

/deep/ .ck.ck-editor__main>.ck-editor__editable {
    height: 100%;
}

.content-title-container {
    padding: 25px 30px;
    font-size: 16px;
    font-weight: 500;
}

.t-upload {
    width: 350px;
}

/deep/ .t-upload__single-file-input .t-upload__trigger {
    margin-left: 10px;
}
</style>