import firebase_admin
import pymysql
import sys
from firebase_admin import credentials
from firebase_admin import storage
# 한글 폰트 패스로 지정
from io import BytesIO
from wordcloud import WordCloud, STOPWORDS


def analyze_for_all(survey_document_id):
    sourceConnect = pymysql.connect(
        host='localhost',
        port=3306,
        user='root',
        password='admin',
        db='surveydb'
    )

    # SQL 예제 : SQL 테이블 둘러보고 다 가져오기
    sourceCursor = sourceConnect.cursor()

    rdb = 'show tables'
    sourceCursor.execute(rdb)

    resultSource = sourceCursor.fetchall()

    # print('Results before insert in source mysql database ', resultSource)
    #     select question_id from question_document where survey_document_id=1 AND question_type = 0;
    #     select check_answer from question_answer where check_answer_id=3 AND question_type = 0;

    temp = resultSource
    for i in range(0, len(temp)):
        rdb = 'select * from ' + ''.join(resultSource[i])
        sourceCursor.execute(rdb)
        resultSources = sourceCursor.fetchall()
        # print(resultSources)
    # print()
    # 끝
    rdb = f'select question_id from question_document where question_type = 0 AND survey_document_id = ' + survey_document_id

    sourceCursor.execute(rdb)
    resultSources = sourceCursor.fetchall()
    # print("첫번째 sql")
    # print(resultSources)
    temp = []
    for i in resultSources:
        temp.append(i[0])
    tempResult = []

    print(temp)
    print(temp[0])

    for i in temp:
        rdb = f'select check_answer from question_answer where check_answer_id={i} AND question_type = 0'
        sourceCursor.execute(rdb)
        resultSources = sourceCursor.fetchall()
        result = ', '.join([item[0] for item in resultSources])
        print(result)
        # wordCloud 저장
        word(result, i, survey_document_id)

    # return resultSources


def word(str, question_document_id, survey_document_id):
    spwords = set(STOPWORDS)  # 제외할 단어
    # spwords.add('내가')  # 제외하고 싶은 단어 추가
    # Firebase 초기화
    cred = credentials.Certificate("secretKey.json")
    firebase_admin.initialize_app(cred, {'storageBucket': 'swave-ba582.appspot.com'})

    # WordCloud 생성
    wc = WordCloud()
    # 워드 클라우드 생성 코드...

    # 이미지 파일로 저장
    wc = WordCloud(max_font_size=200, stopwords=spwords, font_path='C:/Windows/Fonts/H2MJRE',
                   background_color='white', width=800, height=800)
    print(str)
    wc.generate(str)

    image_data = wc.to_image()
    image_stream = BytesIO()
    image_data.save(image_stream, format='PNG')
    image_stream.seek(0)

    # Firebase Storage에 이미지 업로드
    bucket = storage.bucket()
    blob = bucket.blob(f'wordcloud/{survey_document_id}/{question_document_id}.jpg')
    blob.upload_from_file(image_stream, content_type='image/png')

    # 업로드된 이미지의 URL 가져오기
    image_url = blob.public_url

    print("WordCloud 이미지가 업로드되었습니다. URL:", image_url)


def main(id):
    input_param = analyze_for_all(id)
    return input_param


if __name__ == '__main__':
    result = main(sys.argv[1])
    exit(result)

# apriori 구현 이후 필요시 사용할 문항별 디마스킹 (원상복구) 방법 : slice [2: ] 하기
