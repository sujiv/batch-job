package com.altimetrik.ocrbatch.service;

import com.altimetrik.ocrbatch.entity.ApplicationDetails;
import com.altimetrik.ocrbatch.entity.FileStorage;
import com.altimetrik.ocrbatch.repository.FileStorageRepository;
import com.altimetrik.ocrbatch.utils.Utils;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

@Service
public class GrossPayrollFormProcessingService {

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private Tesseract tesseract;

    public ApplicationDetails processGrossPayroll(ApplicationDetails appDetails, FileStorage fileStorage) throws IOException, TesseractException, ParseException {

        BufferedImage bufferedImage = Utils.createImageFromBytes(fileStorage.getGrossPayroll());
        String result = tesseract.doOCR(bufferedImage);

        String[] lines = result.split("\n");
        System.out.println("SIZE: " + lines.length);

        String grossTotals = lines[28];
        String[] lineWords = grossTotals.split(" ");
        System.out.println(grossTotals);
        String sbaGrossPay = lineWords[1];
        NumberFormat format = NumberFormat.getCurrencyInstance();

        DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
        nf.setParseBigDecimal(true);
        BigDecimal bd = (BigDecimal) nf.parse("2,6");

        System.out.println("sbaGrossPay " + sbaGrossPay);
        Number number1 = format.parse(sbaGrossPay);
        System.out.println("sbaGrossPay : " +number1.toString());
//        appDetails.set


        String empStateLocalTaxes = lineWords[2];
//        NumberFormat format = NumberFormat.getCurrencyInstance();
        Number number = format.parse(empStateLocalTaxes);

        System.out.println("empStateLocalTaxes : " +number.toString());

        appDetails.setPaymentEmployerPayrollTaxesStateLocal(Double.valueOf(number.toString()));

        String empBenifts = lineWords[4].replaceAll("[^0-9]", "");
        System.out.println("empBenifts : " +empBenifts);
        appDetails.setPaymentRetirementBen(Double.valueOf(empBenifts));

        String payrollCost = lineWords[5].replaceAll("[^0-9]", "");
        System.out.println("payrollCost : " +payrollCost);
        appDetails.setPrior12MnthsCumQualifyingPayrollCost(Double.valueOf(payrollCost));

        String AvgTotals = lines[29];
        System.out.println(AvgTotals);
        String[] AvgTotalWords = AvgTotals.split(" ");
        String AvgVal = AvgTotalWords[1];
        Number number3 = format.parse(AvgVal);
        System.out.println("AvgVal : " +number3);
        appDetails.setAvgMonthlyPayrollcosts(Double.valueOf(number3.toString()));


        appDetails.setMultiplier2dot5(appDetails.getAvgMonthlyPayrollcosts()* 2.5d);


        if(appDetails.getMultiplier2dot5() > 10000000.00)
            appDetails.setPPP_LoadAmntLesserOfCalcOr10Mil(10000000.00);
        else
            appDetails.setPPP_LoadAmntLesserOfCalcOr10Mil(appDetails.getMultiplier2dot5());

        return appDetails;
    }

}
