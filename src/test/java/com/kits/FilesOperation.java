package com.kits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

public class FilesOperation {

	public static String propertiesFilePath = "C://mySuperWork//RestAssuredAPITest//settings.properties";
	public static String globalToken = null;
	public static HashMap<String, String> context= new HashMap <String, String>();

	public static void readWriteExcel(String filePath, String fileNames, String sheetName, String reportName,
									  int waitTime, int retryCount)
			throws IOException {

		//String[] fileName = System.getProperty("env.sheetName").split(",");

		String[] fileName = fileNames.split(",");
		ExtentReports extent = null;
		XSSFWorkbook workbook = null;
		FileInputStream inputStream = null;

//		File directory = new File("C://Users//akawadhw//Eclipse Projects//RestAssuredAPITest1//RestAssuredAPITest1//src//test//resources//Reports");
		File directory = new File("C://mySuperWork//RestAssuredAPITest//src//test//resources//Reports");
		if (! directory.exists()){
			directory.mkdir();
		}

		ExtentHtmlReporter reporter = new ExtentHtmlReporter(directory+"/"+reportName+".html");
		extent = new ExtentReports();
		extent.attachReporter(reporter);

		try{

			for(int m=0; m<fileName.length; m++) {

				String nameOfFile = fileName[0];
				File file = new File(filePath+"/"+fileName[m]);
				inputStream = new FileInputStream(file);
				String reqUrl = null;
				String method = null;
				Double expectedCode = null;
				XSSFCell cell = null;
				int startRowIndex = 0;

				String fileExtensionName = fileName[m].substring(fileName[m].indexOf("."));
				if (fileExtensionName.equals(".xlsx")) {
					workbook = new XSSFWorkbook(inputStream);

					// Retrieving the number of sheets in the Workbook
					System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
					for(Sheet sheet: workbook) {
						String nameOfSheet = sheet.getSheetName();
						System.out.println("=> " + sheet.getSheetName());

						if(sheet.getRow(0).getCell(1).getStringCellValue().equalsIgnoreCase("False")) {
							System.out.println("Not executing the sheet => " + sheet.getSheetName());
						} else {
							String testCaseName = sheet.getRow(0).getCell(1).toString();
							// Find the index of the row containing word "Steps"
							String toFind = "Steps";
							outerloop:
							for (Row row : sheet) {
								for (Cell cell1 : row) {
									//String value = cell1.getStringCellValue();
									DataFormatter dataFormatter = new DataFormatter();
									String value = dataFormatter.formatCellValue(cell1);
									if(toFind.equals(value)){
										startRowIndex = cell1.getRowIndex();
										break outerloop;
									}
								}
							}

							// If Steps word is found, starting row for executing API is startRowIndex+1
							if(startRowIndex != 0){
								System.out.println("Starting row to execute APIs is: " +(startRowIndex+1));
							}else{
								System.out.println("There is no Steps header in whole sheet: " +sheet);
							}

							//Find the rows having word Token in the first column and put the corresponding keys and values in context map
							for(int i=0; i< startRowIndex; i++){
								String value = null;
								try{
									value = sheet.getRow(i).getCell(0).toString();
									if(value.equals("Token")){
										context.put(sheet.getRow(i).getCell(1).toString(), sheet.getRow(i).getCell(2).toString());
									}
								}catch(Exception e){

								}
							}

							for(int i = startRowIndex+1; i <= sheet.getLastRowNum(); i++) {

								reqUrl = sheet.getRow(i).getCell(1).toString();
								method = sheet.getRow(i).getCell(2).toString();
								String stepName = sheet.getRow(i).getCell(0).toString();

								ExtentTest logger2 = extent.createTest(method + " " + reqUrl + "", "<br /><b>TestCaseName: " + testCaseName+ "<br /><b>FileName: " + nameOfFile+ "<br>SheetName:" +nameOfSheet);

								if(stepName.equalsIgnoreCase("ValidateDB")) {
									String expectedDBCollection = sheet.getRow(i).getCell(9).toString();
									String expectedDBResponse = sheet.getRow(i).getCell(10).toString();
									String actualDBResponse = validateDatabase(expectedDBCollection);
									if(!expectedDBResponse.equalsIgnoreCase("N/A")) {
										JSONObject actualDBResponseObj =new JSONObject(actualDBResponse);
										JSONObject expectedDBResponseObj = new JSONObject(expectedDBResponse);
										boolean areTheyEqual = sendRequest2.compareJsons(actualDBResponseObj, expectedDBResponseObj);
										if(areTheyEqual) {
											System.out.println("Database values are matched");
										}else {
											System.out.println("Database values are not matched");
											if(!areTheyEqual){
												logger2.log(Status.FAIL, "<span style='color:red;'>" +
														" <b>Failed reason: </b>DB validation failed <br />"
														+ "</span>"
														+ "<br /><b>Actual DB response</b> " + actualDBResponseObj
														+ "<br /><b>Expected DB response: </b> " + expectedDBResponse
												);
											}
										}
									}
								}
								else if ((stepName.equalsIgnoreCase("kafka")) ) {
									System.out.println("Kafka");
									String kafkaBroker = prop.getProperty("kafka.broker.url");
									String autoOffsetResetConfig= prop.getProperty("kafka.offset.reset.config");

									String topicName = sheet.getRow(i).getCell(18).toString();
									System.out.println(sheet.getRow(i).getCell(19).toString());
									String expectedJson = sheet.getRow(i).getCell(20).toString();
									System.out.println(expectedJson);

									KafkaTest test = new KafkaTest(kafkaBroker, autoOffsetResetConfig, topicName, "group_id");
									test.checkMsg(expectedJson);
//									test.testKafkaProducerAndConsumer();

								} else {

									reqUrl = sheet.getRow(i).getCell(1).toString();
									method = sheet.getRow(i).getCell(2).toString();

									String requestType=sheet.getRow(i).getCell(13).toString();
									System.out.println(requestType);

									if(!requestType.equals("XML")) {
										if (context.get("token") == null) {
											String authApiURL = prop.getProperty("AuthenticationAPIUrl");
											String token = null;//sendRequest.hitUserAuthAPI(authApiURL);
											context.put("token", token);
										} else {
											System.out.println("Token is already generated");
										}
									}

									String excelRetryCount = sheet.getRow(i).getCell(16).toString();
									String excelWaitTime = sheet.getRow(i).getCell(15).toString();
									int j = 0;

									int RETRY_COUNT;
									int WAIT_TIME;
									RETRY_COUNT = (!excelRetryCount.equalsIgnoreCase("N/A")) ? (int)(Double.parseDouble(excelRetryCount)) : retryCount;
									WAIT_TIME = (!excelWaitTime.equalsIgnoreCase("N/A")) ? (int)(Double.parseDouble(excelWaitTime)) : waitTime;

									while (j <= RETRY_COUNT) {
										if(sendRequest2.testResponse(
												reqUrl,
												method,
												Double.parseDouble(sheet.getRow(i).getCell(7).toString()),
												sheet.getRow(i).getCell(6).toString(),
												sheet.getRow(i).getCell(4).toString(),
												sheet.getRow(i).getCell(5).toString(),
												sheet.getRow(i).getCell(3).toString(),
												sheet.getRow(i).getCell(8).toString(),
												sheet.getRow(i).getCell(11).toString(),
												sheet.getRow(i).getCell(12).toString(),
												sheet.getRow(i).getCell(13).toString(),
												sheet.getRow(i).getCell(14).toString(),
												nameOfFile,
												nameOfSheet,
												extent,context,logger2) == 0) {
											break;
										} else {
											System.out.println("API failed");
											TimeUnit.SECONDS.sleep(WAIT_TIME);
											++j;
											if (j == RETRY_COUNT) {

											}
										}
									}
								}
							}
						}
					}
				}

			} // workbooks for loop end
		}catch(Exception e){
			System.out.println(e.getMessage());
		}finally{
			extent.flush();
			workbook.close();
			inputStream.close();
		}

	}

	public FilesOperation excelOp;
	public static Properties prop;
	public static InputStream input;

	public static void readPropertiesFile() {
		prop = new Properties();
		try {
			input = new FileInputStream(propertiesFilePath);
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testAPIs() throws IOException {

		readPropertiesFile();
		String testDataExcelPath = prop.getProperty("TestDataExcelPath");
		String testDataExcel = prop.getProperty("TestDataExcelName");
		String sheetName = prop.getProperty("SheetName");
		String reportName = prop.getProperty("ReportName");
		int waitTime = Integer.parseInt(prop.getProperty("waitTime"));
		int retryCount = Integer.parseInt(prop.getProperty("retryCount"));


		readWriteExcel(testDataExcelPath, testDataExcel, sheetName, reportName, waitTime, retryCount);

	}

	public static String validateDatabase(String expectedDBCollection) {
		String actualDBResponse= null;
		return actualDBResponse ;

	}
}