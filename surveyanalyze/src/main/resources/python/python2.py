import pandas as pd
import pymysql
import sys
from mlxtend.frequent_patterns import apriori
from mlxtend.preprocessing import TransactionEncoder


def analyze_for_all(survey_document_id):
    sourceConnect = pymysql.connect(
        host='localhost',
        port=3306,
        user='root',
        password='admin',
        db='answerdb'
    )

    # SQL 예제 : SQL 테이블 둘러보고 다 가져오기
    sourceCursor = sourceConnect.cursor()

    rdb = 'show tables'
    sourceCursor.execute(rdb)

    resultSource = sourceCursor.fetchall()

    # print('Results before insert in source mysql database ', resultSource)

    temp = resultSource
    for i in range(0, len(temp)):
        rdb = 'select * from ' + ''.join(resultSource[i])
        sourceCursor.execute(rdb)
        resultSources = sourceCursor.fetchall()
        # print(resultSources)
    # print()
    # 끝
    rdb = f'SELECT survey_answer_id FROM SURVEY_ANSWER where survey_document_id=' + survey_document_id

    sourceCursor.execute(rdb)
    resultSources = sourceCursor.fetchall()
    # print("첫번째 sql")
    # print(resultSources)
    temp = []
    for i in resultSources:
        temp.append(i[0])
    tempResult = []

    # print(resultSources)
    for i in temp:
        # 주관식은 skip // choice type 으로 거르기
        rdb = f'SELECT check_answer_id FROM QUESTION_ANSWER WHERE survey_answer_id={i} AND question_type <> 0'
        sourceCursor.execute(rdb)
        resultSources = sourceCursor.fetchall()

        answer = []
        for t in resultSources:
            answer.append(t[0])
        tempResult.append(answer)
    resultSources = tempResult
    # print(resultSources)

    totalCount = len(resultSources)
    # apriori 구현을 위하여 문항별 마스킹
    for i in range(0, len(resultSources)):
        for t in range(0, len(resultSources[i])):
            resultSources[i][t] = f'{t}_{resultSources[i][t]}'
    answers = []
    for i in range(0, len(resultSources[0])):
        # i = i번째 문항답. 이를 모두 추려야함
        answers.append([])
        for t in range(0, len(resultSources)):
            answers[i].append(resultSources[t][i])
    counterForAnswers = []
    for i in range(0, len(answers)):
        counterForAnswers.append({})
        for t in answers[i]:  # 답변별로 분류
            if t in counterForAnswers[i]:
                counterForAnswers[i][t] += 1
            else:
                counterForAnswers[i][t] = 1
    answerKind = []
    for i in range(0, len(counterForAnswers)):
        answerKind.append(list(counterForAnswers[i].keys()))

    responsePerAnswer = []  # 모든 문항별로 분류된 매우 큰 리스트

    for i in range(0, len(answerKind)):
        responsePerAnswer.append([])  # 문항별 큰 칸 (두번째 층)
        for k in range(0, len(answerKind[i])):
            responsePerAnswer[i].append([])
            for p in range(0, len(resultSources)):  # 모든 문항 한번씩 순회. 필요한 것: 비교
                if resultSources[p][i] == answerKind[i][k]:
                    responsePerAnswer[i][k].append(resultSources[p])
    ultimateApriori = []
    for i in range(0, len(responsePerAnswer)):
        ultimateApriori.append([])
        for k in range(0, len(responsePerAnswer[i])):
            ultimateApriori[i].append([])
            te = TransactionEncoder()
            te_ary = te.fit(responsePerAnswer[i][k]).transform(responsePerAnswer[i][k])
            df = pd.DataFrame(te_ary, columns=te.columns_)
            frequent_itemsets = apriori(df, min_support=0.2, use_colnames=True)
            frequent_itemsets = frequent_itemsets.nlargest(10, 'support')
            tempList = []
            for test, row in frequent_itemsets.iterrows():
                support = row['support']
                itemset = list(row['itemsets'])
                tempList.append([support, itemset])
            ultimateApriori[i][k].append(tempList)

    result_list = []
    for i in range(0, len(ultimateApriori)):
        for t in range(0, len(ultimateApriori[i])):
            select = []
            for p in range(0, len(ultimateApriori[i][t])):
                count = 0
                select.append(answerKind[i][t][-1])
                select1 = []

                for p1 in range(0, len(ultimateApriori[i][t][p])):
                    if count >= 3:
                        break
                    if len(ultimateApriori[i][t][p][p1][1]) > 1:
                        continue
                    elif (ultimateApriori[i][t][p][p1][1][0][-1] == answerKind[i][t][-1]):
                        continue
                    else:
                        count += 1
                        select.append([ultimateApriori[i][t][p][p1][0], ultimateApriori[i][t][p][p1][1][0][-1]])

            result_list.append(select)
    if (len(result_list) == 0):
        print("문항 1개 오류")
    else:
        print(result_list)

    return result_list


def main(id):
    input_param = analyze_for_all(id)
    return input_param


if __name__ == '__main__':
    result = main(sys.argv[1])
    exit(result)

# apriori 구현 이후 필요시 사용할 문항별 디마스킹 (원상복구) 방법 : slice [2: ] 하기
