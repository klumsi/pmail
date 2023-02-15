import pandas as pd
import joblib
import time
from ast import literal_eval
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.naive_bayes import MultinomialNB


data = pd.read_csv("./data/data.csv")

X = data["text"]
y = data["spam"]

seed = int(time.time())

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=seed)

corpus_train = [' '.join(literal_eval(sample)) for sample in X_train]
corpus_test = [' '.join(literal_eval(sample)) for sample in X_test]

vectorizer = CountVectorizer()
corpus_train = vectorizer.fit_transform(corpus_train)

mnb = MultinomialNB()
mnb.fit(corpus_train, y_train)

joblib.dump(vectorizer, "./model/vectorizer.joblib")
joblib.dump(mnb, "./model/mnb.joblib")

corpus_test = vectorizer.transform(corpus_test)

pred = mnb.predict(corpus_test)

count = 0

for x, y in zip(pred, list(y_test)):
    if (x == y):
        count += 1

print("accuracy:", (count / len(y_test)) * 100, '%')
