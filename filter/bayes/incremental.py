import os
import email
import jieba
import re
import pandas as pd
from nltk.corpus import stopwords


STAGE_HAM_DATASET_PATH = './dataset/raw/stage/ham'
STAGE_SPAM_DATASET_PATH = './dataset/raw/stage/spam'

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


def process(body):
    stop_words = set(stopwords.words("english") + stopwords.words("chinese"))
    text = re.sub("[^\u4e00-\u9fa5]+", '', body).lower()
    text_list = jieba.lcut(text)
    text_list = [
        word for word in text_list if word not in stop_words and len(word) > 1]
    return text_list


ham_files = os.listdir(STAGE_HAM_DATASET_PATH)
spam_files = os.listdir(STAGE_SPAM_DATASET_PATH)

corpus = []
labels = []

for file in ham_files:
    with open(STAGE_HAM_DATASET_PATH + '/' + file, 'r') as raw_file:
        raw_text = raw_file.read()
        body = get_body(raw_text)
        corpus.append(process(body))
        labels.append(0)

for file in spam_files:
    with open(STAGE_SPAM_DATASET_PATH + '/' + file, 'r') as raw_file:
        raw_text = raw_file.read()
        body = get_body(raw_text)
        corpus.append(process(body))
        labels.append(1)

data = pd.DataFrame({"text": corpus, "spam": labels})
old_data = pd.read_csv("./data/data.csv")[["text", "spam"]]

new_data = pd.concat([old_data, data], ignore_index=True)
new_data.to_csv("./data/data.csv")
