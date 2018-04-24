/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.node4jvssql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

/**
 *
 * @author Kasper
 */
public class DBAccessor {

    private Connector con = new Connector();
    private Connection sqlCon;
    private Driver driver;
    private Session session;
    private List<String> testNames;

    DBAccessor() {
       driver = con.connectToNeo4JDB();
       session = driver.session();
       testNames = Arrays.asList("Jeanie Mountcastle","Kindra Ryser","Royce Fadely","Nevada Albarran","Gayla Brase",
                                  "Wilhelmina Beltram","Ena Walin","Antonette Barthen","Blanche Kuchenbecker","Bibi Sieren",
                                  "Karri Goertzen","Doretta Freytas","Mayra Vitantonio","Casey Phetphongsy","Coletta Mateus",
                                  "Loriann Hnot","Denyse Aukes","Chong Stolte","Corene Eska","Shirly Orpin");
        try {
            sqlCon = con.connectToSQLDB();
        } catch (SQLException ex) {
            Logger.getLogger(DBAccessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runTest() {
        System.out.println("test results in milisec");
        System.out.println("                MySQL              Neo4j       ");
        System.out.println("          average   median    average   median");
        long[] neoResult;
       try {
        long[] result = runSQL("SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='%s'))");
        neoResult = runNeo4J("MATCH ({name:\"%s\"})-[:ENDORSES]->(other) RETURN other.name AS Endorses");
        calculateAndShowResult(result, neoResult, "Depth 1: ");
        
        result =  runSQL("SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id IN (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='%s')))");
        neoResult = runNeo4J("MATCH ({name:\"%s\"})-[:ENDORSES]-(:Person)-[:ENDORSES]->(other_other) RETURN other_other.name AS Endorses");
        calculateAndShowResult(result,neoResult, "Depth 2: ");
        
        result = runSQL("SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id IN (SELECT endorses_id FROM endorses  WHERE endorses.id in (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='%s'))))");
        neoResult = runNeo4J("MATCH ({name:\"%s\"})-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]->(other_other_other) RETURN other_other_other.name AS Endorses");
        calculateAndShowResult(result,neoResult, "Depth 3: ");
        
        result = runSQL("SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  \n" +
										 "WHERE endorses.id IN (SELECT endorses_id FROM endorses  \n" +
																"WHERE endorses.id in (SELECT endorses_id FROM endorses  \n" +
																					  "WHERE endorses.id in (SELECT endorses_id FROM endorses  \n" +
																											"WHERE endorses.id =(SELECT id FROM users WHERE name='%s')))))");
        neoResult = runNeo4J("MATCH ({name:\"%s\"})-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]->(other_other_other_other) RETURN other_other_other_other.name AS Endorses");
        calculateAndShowResult(result,neoResult, "Depth 4: ");
        result = runSQL("SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  \n" +
										 "WHERE endorses.id IN (SELECT endorses_id FROM endorses  \n" +
																"WHERE endorses.id in (SELECT endorses_id FROM endorses  \n" +
																					  "WHERE endorses.id in (SELECT endorses_id FROM endorses  \n" +
																											"WHERE endorses.id in (SELECT endorses_id FROM endorses  \n" +
																													 "WHERE endorses.id = (SELECT id FROM users WHERE name='%s'))))))");
        neoResult = runNeo4J("MATCH ({name:\"%s\"})-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]-(:Person)-[:ENDORSES]->(other_other_other_other_other) RETURN other_other_other_other_other.name AS Endorses");
        calculateAndShowResult(result,neoResult, "Depth 5: ");
} catch (SQLException ex) {
            Logger.getLogger(DBAccessor.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            session.close();
            driver.close();
            try {
                sqlCon.close();
            } catch (SQLException ex) {
                Logger.getLogger(DBAccessor.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
    }

    private long[] runNeo4J(String Cypher) {
        long[] time = new long[20];
        int count = 0;
        String temp = Cypher;
        for (String name : testNames) {
            Cypher = String.format(Cypher, name);
            long nanoTime = System.nanoTime();
            StatementResult result = session.run(Cypher);
            nanoTime = System.nanoTime() - nanoTime;
            time[count] = nanoTime;
            count++;
            Cypher = temp;
        }
        return time;
    }
    
    private long[] runSQL(String sql) throws SQLException{
        
        long[] time = new long[20];
        int count = 0;
        String temp = sql;
        for (String name : testNames) {
            sql = String.format(sql, name);
            long nanoTime = System.nanoTime();
            sqlCon.createStatement().executeQuery(sql);
            nanoTime = System.nanoTime() - nanoTime;
            time[count] = nanoTime;
            count++;
            sql = temp;
        }
        Arrays.sort(time);
        return time;
        
       //1 SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='Jeanie Mountcastle'))
       //2 SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id IN (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='Jeanie Mountcastle')))
       //3 SELECT users.name FROM users WHERE id IN (SELECT endorses_id FROM endorses  WHERE endorses.id IN (SELECT endorses_id FROM endorses  WHERE endorses.id in (SELECT endorses_id FROM endorses  WHERE endorses.id = (SELECT id FROM users WHERE name='Jeanie Mountcastle'))))
    }
    
    private void calculateAndShowResult(long[] result, long[] neoResult, String text){
         long total = 0;
         long totalNeo = 0;
         for(long l : result){
             total += (l/1000000);
         }
         
         for(long l : neoResult){
             totalNeo += (l/1000000);
         }
         System.out.println(text + " " + (total/20) + "    " + (result[9]/1000000) + "     " + (totalNeo/20) + "    " + (neoResult[9]/1000000));
    }
}
