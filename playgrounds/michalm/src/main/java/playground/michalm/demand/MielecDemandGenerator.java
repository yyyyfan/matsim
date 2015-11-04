/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.michalm.demand;

import java.io.IOException;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import pl.poznan.put.util.array2d.Array2DUtils;


public class MielecDemandGenerator
{
    public static void main(String[] args)
        throws ConfigurationException, IOException, SAXException, ParserConfigurationException
    {
        String dirName = "D:\\PP-rad\\taxi\\mielec-2-peaks\\";
        String networkFileName = dirName + "network.xml";
        String zonesXMLFileName = dirName + "zones.xml";
        String zonesShpFileName = dirName + "GIS\\zones_with_no_zone.SHP";
        String odMatrixFileName = dirName + "odMatrix.dat";
        String plansFileName = dirName + "plans.xml";
        String idField = "NO";

        String taxiFileName = dirName + "taxiCustomers_07_pc.txt";

        // double hours = 2;
        // double flowCoeff = 1;
        // double taxiProbability = 0;

        double hours = 1;
        double[] flowCoeff = { 0.2, 0.2, 0.4, 0.8, 0.4, 0.2, 0.2 };
        double taxiProbability = 0.07;

        ODDemandGenerator dg = new ODDemandGenerator(networkFileName, zonesXMLFileName,
                zonesShpFileName, idField);

        double[][] odMatrix = dg.readODMatrix(odMatrixFileName);
        double[][] odMatrixTransposed = Array2DUtils.transponse(odMatrix);

        double startTime = 0;
        
        //morning peak
        for (int i = 0; i < flowCoeff.length; i++) {
            dg.generateSinglePeriod(odMatrix, hours, flowCoeff[i], taxiProbability, startTime);
            startTime += 3600 * hours;
        }
        
        //symmetric evening peak
        for (int i = 0; i < flowCoeff.length; i++) {
            dg.generateSinglePeriod(odMatrixTransposed, hours, flowCoeff[i], taxiProbability,
                    startTime);
            startTime += 3600 * hours;
        }

        dg.write(plansFileName);
        dg.writeTaxiCustomers(taxiFileName);
    }
}