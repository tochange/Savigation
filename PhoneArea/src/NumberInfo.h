#ifndef _PHONEINFO_
#define _PHONEINFO_
#include "Array.h"
#include <string.h>
//#include <iostream>

//using namespace std;

//find the max length of the city name
//when write the city name on the file,set every city name's length to the "MaxCityLength"
//so when we try to find city,can quickly find the result like finding result in an array
const int MaxCityLength = 25;

//data like
///1315160,cityName1
///1315161,cityName1
///1315162,cityName2
///1315163,cityName2
///1315164,cityName2
///1315165,cityName2
///1315166,cityName2
//will compress to 13151,160,0 and 13151,462,1
//
//
class NumberInfoCompress{

public:
	NumberInfoCompress():m_before(0),m_after(0),m_cityIndex(0){
	}
	NumberInfoCompress(int begin,unsigned short skip,unsigned short cityIndex)
	{
		setBegin(begin);
		setSkip(skip);
		m_cityIndex = cityIndex;
	}
	int getBegin(){
		int lastTwoNumInAfter = m_after - getNumExceptLastTwo() * 100;
		return m_before * 100 + lastTwoNumInAfter; 
	}
	unsigned short getNumExceptLastTwo(){return m_after * 0.01;}
	unsigned short getSkip(){ return getNumExceptLastTwo(); }
	unsigned short getCityIndex()const{ return m_cityIndex; }
	unsigned short getLastTwoNum(int number){
		int exceptLastTwoNum = number * 0.01;
		return (number - exceptLastTwoNum * 100);
	}
	void setBegin(int& number){
		int lastTwoNum = getLastTwoNum(number);
		m_before = number * 0.01;
		m_after = getSkip() * 100 + lastTwoNum;
	}
	void setSkip(unsigned short skip){
	  m_after =	skip * 100 + getLastTwoNum(m_after);
	}
	void setCityIndex(unsigned short& city){m_cityIndex = city;}
private:
	unsigned short m_before;//store the 5 digit of the number,example: it store 136700 of 1367002
	
	unsigned short m_after;//store skip and last two digit of the number,
                        //example:102,means 1 is the skip,02 is the last two digit of the number(1367002)
	unsigned short  m_cityIndex;

};



class NumberInfoAction{
private:
	// -------------------------------------------------------
	//  Name:         GetCityIndexByCityName
	//  Description:  Get city index from the array<char*>, if can not 
	//                the city, insert the city, then, return the index
	//  Arguments:    city name
	//  Return Value: city index
	// -------------------------------------------------------
	int GetCityIndexByCityName(char* cityName);
	void WriteCities( FILE* outFile );
	void WriteRecords(FILE* inFile, int &phoneInfoCompressCount, FILE* outFile );
	char* DoFindResultThing( FILE* file,const int& phoneInfoCompressCount,const NumberInfoCompress &infoMiddle ); 

public:
	NumberInfoAction():cities(500),preCityIndex(0){

	}
	~NumberInfoAction(){
		delete cities;
	}
	//vector<char*> getLocationInfo() const{return cities;}
	Array<char*> getLocationInfo() const{return cities;}

	// -------------------------------------------------------
	//  Name:         GetCityNameByNumber
	//  Description:  input the phone number, find the city in the binary file 
	//  Arguments:    bFileName:the binary file name,number: the phone number
	//  Return Value: city name,not find return ""
	// -------------------------------------------------------
	char* GetCityNameByNumber(const char* bFileName,const int& number);
    
	// -------------------------------------------------------
	//  Name:         ChangeTxtToBinary
	//  Description:  Read every line in txt file, convert it to special customized format
	//                binary file.
	//                binary file content: count of total records, records, cities  
	//  Arguments:    txt file name, binary file name
	//  Return Value: true means success
	// -------------------------------------------------------
	bool ChangeTxtToBinary(const char* inFileName,const char* outFileName);

private:
   //vector<char*> cities;
   Array<char*> cities;
   int preCityIndex ;
};


int NumberInfoAction::GetCityIndexByCityName(char* cityName){

	//try to use the last search cityIndex to quick find.
	int maxIndex = cities.size() - 1;
	if(preCityIndex <= maxIndex && strcmp(cities[preCityIndex],cityName) == 0){
		return preCityIndex;
	}

	int result = -1;
	//try to find in existing cities
	for(int i = 0; i < cities.size(); ++i){
		if(strcmp(cities[i],cityName) == 0){
			result = i;
			break;
		}
	}
	//not found
	if(result == -1){
		cities.push_back(cityName);
		result = cities.size() - 1;
	}
	//store the result
	preCityIndex = result;
	return result;
}

void NumberInfoAction::WriteCities( FILE* outFile ){
	//begin to write city name
	fseek(outFile,0,SEEK_END);
	int max = 0;
	for(int i = 0; i != cities.size(); ++i){
		char* location = cities[i];
		int length = strlen(location) + 1;
		if(length > max){// try to find the "MaxCityLength"
			max = length;
		}
		//just write every city in "MaxCityLength" size
		//when we try to find a city, like find an element in an array,very quickly.
		fwrite(location,MaxCityLength,1,outFile);
	}
	//cout << "max:" << max << endl;
}

void NumberInfoAction::WriteRecords(FILE* inFile, int &phoneInfoCompressCount, FILE* outFile ){
	int number;
	unsigned short cityIndex;
	NumberInfoCompress pre;
	char* firstCityName = new char[MaxCityLength];
	fscanf(inFile,"%d,%s",&number,firstCityName);
	cityIndex = GetCityIndexByCityName(firstCityName);
	//firstly,we get the first record
	pre = NumberInfoCompress(number,0,cityIndex);
	while(!feof(inFile)){
		//start from second record
		char* cityName = new char[MaxCityLength];
		fscanf(inFile,"%d,%s",&number,cityName);
//		printf("No:%d,Name:%s\n",number,cityName);
		cityIndex = GetCityIndexByCityName(cityName);
		if(number - (pre.getBegin() + pre.getSkip()) == 1
			&& cityIndex == pre.getCityIndex()){
				pre.setSkip(number - pre.getBegin());//combine records to one compressed record
		}else{//new compressed record
			++phoneInfoCompressCount;
			fwrite(&pre,sizeof(NumberInfoCompress),1,outFile);
			pre = NumberInfoCompress(number,
				0,
				cityIndex);
		}
	}
	//remember write the last record
	fwrite(&pre,sizeof(NumberInfoCompress),1,outFile);
	++phoneInfoCompressCount;
}


char* NumberInfoAction::DoFindResultThing( FILE* file,const int& phoneInfoCompressCount,const NumberInfoCompress &infoMiddle ) 
{

	int totalOffset = sizeof(int) + phoneInfoCompressCount*sizeof(NumberInfoCompress) + infoMiddle.getCityIndex() * MaxCityLength;
	//put the read point at the result
	fseek(file,totalOffset,SEEK_SET);
	char* location = new char[MaxCityLength];
    fread(location,MaxCityLength,1,file);
	fclose(file);
	return location;

}

char* NumberInfoAction::GetCityNameByNumber(const char* bFileName,const int& number){
	FILE* file = 0;
	file = fopen(bFileName,"rb");
	if(file == 0)
		return (char*)"";

	int phoneInfoCompressCount = 0;
	//get total phoneInfoCompress count
	fread(&phoneInfoCompressCount,sizeof(int),1,file);

	int left = 0, right = phoneInfoCompressCount - 1;
	NumberInfoCompress infoMiddle;
	//begin binary search
	while(left <= right){
		int middle = (left + right) / 2;
		//put the write point in the  middle phoneInfoCompress 
		fseek(file,sizeof(int) + middle * sizeof(NumberInfoCompress),SEEK_SET);		
		fread(&infoMiddle,sizeof(NumberInfoCompress),1,file);

		if(number < infoMiddle.getBegin()){
			right = middle - 1;
		}else if(number > (infoMiddle.getBegin() + infoMiddle.getSkip())){
			left = middle + 1;
		}else{// find the result
			return DoFindResultThing(file, phoneInfoCompressCount, infoMiddle);
		}
	}
	fclose(file);
	return (char*)"";
}

bool NumberInfoAction::ChangeTxtToBinary(const char* inFileName,const char* outFileName){
	FILE* inFile = 0;
	inFile = fopen(inFileName,"rb");
	if(inFile == 0)
		return false;
	FILE* outFile = 0;
	outFile = fopen(outFileName,"wb");
	if(outFile == 0)
		return false;

	int phoneInfoCompressCount = 0;
	//firstly, write the count of total phoneInfoCompress.
	fwrite(&phoneInfoCompressCount,sizeof(int),1,outFile);

	//secondly, write all the records
	WriteRecords(inFile, phoneInfoCompressCount, outFile);

	//thirdly, rewrite the phoneInfoCompressCount.
	fseek(outFile,0,SEEK_SET);
	fwrite(&phoneInfoCompressCount,sizeof(int),1,outFile);

	//last, write all the cities
	WriteCities(outFile);

	fclose(inFile);
	fclose(outFile);
	return true;
}

#endif
