/*
 * Copyright 2015 ZSDN Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmf.language;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @brief Unit test that initializes the interoperability test between Java and C++
 * @details  The InterLanguageTest-class starts the tests that check the interoperability of the JMF(Java) and the ZMF(C++).
 * Therefore the C++-project 'inter-language' at '/tests/inter-language/' needs to be compiled first.
 * This class then first calls the C++-executable asynchronously and afterwards the LanguageTester-class of Java which controls
 * the Java-part of the test. The Unit Test only asserts the Java-side results to be correct. The results of the C++-Tests are only
 * printed to the console. You may need to turn of the Logger for the console to find them.
 * @author Matthias Blohm
 * @date created on 8/11/15.
 */
public class InterLanguageTest {


    /*
    *Starts the Test interoperability test between Java and C++
     */
    @Test
    public void startTest(){
        //sets the number of module-instances that are started of each type.
        //This means there will be at most 4*numberModules active at the same time
        //tested with 2 <= numberModules <= 25
        int numberModules = 5;
        // waitingTimeout each until subtest is marked as failing (in ms). should be 2000 minimum, needs to be higher for more modules
        int waitingTimeout = 4000;
        LanguageTester l = new LanguageTester(numberModules,waitingTimeout);
        try {
            ProcessBuilder pb = new ProcessBuilder("./inter_language",String.valueOf(numberModules),String.valueOf(waitingTimeout));
            pb.directory(new File("../../tests/inter-language/build"));
            pb.inheritIO();
            Process p = pb.start();
            l.startTest();
            p.waitFor();
            int cResult = p.exitValue();
            if (cResult == 0){
                System.out.println("C++ TESTS: SUCCESS! (all tests successful)");
            } else {
                System.out.println("C++ TESTS: FAILURE! (test "+cResult+" failed and some others may have too - see C++ test result in console)");
            }
            Assert.assertTrue(cResult == 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
