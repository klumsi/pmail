import time
import os
import sqlite3
import email
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from bayes.test import test

MESSAGES_PATH = '/var/lib/maddy/messages'
DB_PATH = '/var/lib/maddy/imapsql.db'


class CreateEventHandler(FileSystemEventHandler):
    def on_created(self, event):
        extKey = getExtKey(event.src_path)
        time.sleep(1)
        mboxId, msgId = getMBoxIdAndMsgId(extKey)[0]
        if (mboxId - 1) % 6 != 0:
            return
        owner = getOwner(extKey)
        body = getBody(owner, msgId)
        if isSpam(body) == False:
            return
        moveToJunk(owner, msgId)


def getExtKey(path):
    return path[-32:]


def getMBoxIdAndMsgId(extKey):
    with sqlite3.connect(DB_PATH) as conn:
        c = conn.cursor()
        sql = 'select mboxId, msgId from msgs where extBodyKey = ?'
        c.execute(sql, (extKey,))
    return c.fetchall()


def getOwner(extKey):
    with sqlite3.connect(DB_PATH) as conn:
        c = conn.cursor()
        sql_1 = 'select uid from extKeys where id = ?'
        sql_2 = 'select username from users where id = ?'
        c.execute(sql_1, (extKey,))
        uid = c.fetchone()
        c.execute(sql_2, uid)
    return c.fetchone()[0]


def getBody(owner, msgId):
    cmd = 'maddy imap-msgs dump --uid {0} INBOX {1}'.format(owner, msgId)
    r = os.popen(cmd)
    mail = email.message_from_string(r.read())
    body = ''
    if mail.is_multipart():
        for part in mail.walk():
            cType = part.get_content_type()
            cDisp = str(part.get('Content-Disposition'))
            if cType == 'text/plain' and 'attachment' not in cDisp:
                body = part.get_payload(decode=True)
                break
    else:
        body = mail.get_payload(decode=True)
    return body.decode()


def isSpam(body):
    return True if test(body)[0] == 1 else False


def moveToJunk(owner, msgId):
    cmd = 'maddy imap-msgs move --uid {0} INBOX {1} Junk'.format(owner, msgId)
    os.system(cmd)


if __name__ == "__main__":
    event_handler = CreateEventHandler()
    observer = Observer()
    observer.schedule(event_handler, MESSAGES_PATH, recursive=True)
    observer.start()
    try:
        while True:
            time.sleep(1)
    finally:
        observer.stop()
        observer.join()
