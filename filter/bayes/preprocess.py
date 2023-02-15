import email
import pandas as pd
import jieba
import re
from nltk.corpus import stopwords
from nltk.tokenize import RegexpTokenizer

EN_DATASET_PATH = './dataset/raw/trec06p'
ZH_DATASET_PATH = './dataset/raw/trec06c'

SPAM = 10
HAM = 10


def get_body(raw_text):
    mail = email.message_from_string(raw_text)
    body = ''
    if mail.is_multipart():
        for part in mail.walk():
            c_type = part.get_content_type()
            if c_type == 'text/plain':
                body = part.get_payload()
                break
    else:
        body = mail.get_payload()
    return body


def process_english_text(text):
    stop_words = set(stopwords.words("english"))
    tokenizer = RegexpTokenizer("[a-z]+")
    token = tokenizer.tokenize(text)
    token = [
        word for word in token if word not in stop_words and len(word) > 1]
    return token


def process_chinese_text(text):
    text = re.sub("[^\u4e00-\u9fa5]+", '', text)
    stop_words = set(stopwords.words("chinese"))
    token = jieba.lcut(text)
    token = [
        word for word in token if word not in stop_words and len(word) > 1]
    return token


def get_corpus():
    corpus = []
    labels = []
    spam_cnt = 0
    ham_cnt = 0

    with open(EN_DATASET_PATH + "/full/index", 'r') as f:
        for line in f:
            line = line.split(' ')
            path = EN_DATASET_PATH + line[1][2:-1]
            if line[0] == 'spam' and spam_cnt >= SPAM / 2:
                continue
            if line[0] == 'ham' and ham_cnt >= HAM / 2:
                continue
            try:
                with open(path, 'r', encoding="utf-8") as raw_file:
                    raw_text = raw_file.read()
                    body = get_body(raw_text)
                    corpus.append(process_english_text(body.lower()))
                    labels.append(1 if line[0] == "spam" else 0)
                    if line[0] == "spam":
                        spam_cnt += 1
                    else:
                        ham_cnt += 1
            except:
                pass

    with open(ZH_DATASET_PATH + "/full/index", 'r') as f:
        for line in f:
            line = line.split(' ')
            path = ZH_DATASET_PATH + line[1][2:-1]
            if line[0] == 'spam' and spam_cnt >= SPAM:
                continue
            if line[0] == 'ham' and ham_cnt >= HAM:
                continue
            try:
                with open(path, 'r', encoding="gb2312") as raw_file:
                    raw_text = raw_file.read()
                    body = get_body(raw_text)
                    corpus.append(process_chinese_text(body))
                    labels.append(1 if line[0] == "spam" else 0)
                    if line[0] == "spam":
                        spam_cnt += 1
                    else:
                        ham_cnt += 1
            except:
                pass

    data = pd.DataFrame({"text": corpus, "spam": labels})
    return data


if __name__ == "__main__":
    get_corpus().to_csv("./data/data.csv")
