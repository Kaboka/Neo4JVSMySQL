/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.node4jvssql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

/**
 *
 * @author Kasper
 */
public class Connector {

    
    public Driver connectToNeo4JDB(){
        Driver driver = GraphDatabase.driver( 
        "bolt://localhost:7687", 
        AuthTokens.basic( "neo4j", "class" ) );
        return driver;
    }
    
    public Connection connectToSQLDB() throws SQLException{
        String url = "jdbc:mysql://localhost:3306/socialnetwork";
        String user = "root";
        String password = "hejsan";
        return DriverManager.getConnection(url, user, password);
    }
    
}
