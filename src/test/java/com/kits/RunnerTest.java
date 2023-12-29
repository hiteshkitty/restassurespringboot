package com.kits;

import java.io.IOException;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

@SpringBootTest
public class RunnerTest {

//	@Test
	public void runTest() throws IOException {
		FilesOperation.testAPIs();
	}

}
