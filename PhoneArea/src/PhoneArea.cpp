//============================================================================
// Name        : PhoneArea.cpp
// Author      : xiaojian（copy）
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
		cout << "输入查询号码小于等于7位.如10086，0755，1367002" << endl;
		int searchNum = 0;
		cin >> searchNum;
		cout << "号码所在城市: " << action.GetCityNameByNumber(fileName, searchNum)
				<< endl;
	}
}

void convertFile(NumberInfoAction& action) {
	char* inFileName = new char[256];
	char* outFileName = new char[256];
	cout << "输入txt文件名带后缀如 txtData.txt" << endl;
	cin >> inFileName;
	cout << "输入2进制文件名带后缀如 AreaData.dat" << endl;
	cin >> outFileName;
	cout << "开始转换..." << endl;
	bool result = action.ChangeTxtToBinary(inFileName, outFileName);
	if (result) {
		cout << "转换结束。" << endl;
		//begin search
		searchNum(action, outFileName);
	} else {
		cout << "转换失败。" << endl;
	}

}
int main() {
	NumberInfoAction action;
	char isConvertFile;
	cout << "是否要把txt文件转换成2进制文件,(y/n)" << endl;
	cin >> isConvertFile;
	if (isConvertFile == 'y') {
		convertFile(action);
	} else {
		searchNum(action, "AreaData.dat");
	}
	return 0;
}
