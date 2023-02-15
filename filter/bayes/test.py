import joblib
import jieba
from nltk.corpus import stopwords


def test(text):
    vectorizer = joblib.load("./bayes/model/vectorizer.joblib")
    mnb = joblib.load("./bayes/model/mnb.joblib")

    text_list = jieba.lcut(text)
    stop_words = set(stopwords.words("english") + stopwords.words("chinese"))
    text_list = [
        word for word in text_list if word not in stop_words and len(word) > 1]

    corpus_test = [' '.join(text_list)]

    corpus_test = vectorizer.transform(corpus_test)

    pred = mnb.predict(corpus_test)

    return pred
