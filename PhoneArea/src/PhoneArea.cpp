//============================================================================
// Name        : PhoneArea.cpp
// Author      : xiaojian��copy��
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include "NumberInfo.h"
#include "Array.h"
using namespace std;
void searchNum(NumberInfoAction& action, const char* fileName) {
	while (true) {
		cout << "�����ѯ����С�ڵ���7λ.��10086��0755��1367002" << endl;
		int searchNum = 0;
		cin >> searchNum;
		cout << "�������ڳ���: " << action.GetCityNameByNumber(fileName, searchNum)
				<< endl;
	}
}

void convertFile(NumberInfoAction& action) {
	char* inFileName = new char[256];
	char* outFileName = new char[256];
	cout << "����txt�ļ�������׺�� txtData.txt" << endl;
	cin >> inFileName;
	cout << "����2�����ļ�������׺�� AreaData.dat" << endl;
	cin >> outFileName;
	cout << "��ʼת��..." << endl;
	bool result = action.ChangeTxtToBinary(inFileName, outFileName);
	if (result) {
		cout << "ת��������" << endl;
		//begin search
		searchNum(action, outFileName);
	} else {
		cout << "ת��ʧ�ܡ�" << endl;
	}

}
int main() {
	NumberInfoAction action;
	char isConvertFile;
	cout << "�Ƿ�Ҫ��txt�ļ�ת����2�����ļ�,(y/n)" << endl;
	cin >> isConvertFile;
	if (isConvertFile == 'y') {
		convertFile(action);
	} else {
		searchNum(action, "AreaData.dat");
	}
	return 0;
}
