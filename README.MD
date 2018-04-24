So from working with this assignment i had to conclude that i would never get done caulating the diffrence in speed when running on the full big dataset.
The big performance hug here was cleary the SQL part. I have been using mySQL and to speed things up i tried adding an index on the user id's and the table with user id and endorsment id.
I also increased the buffer size to 6GB but still i dont feel like i got the performance i wanted
But still if i want to calculate past depth 3 it would take me several hours that i sadly dont have since as i'm writing this its 19:37 on april the 24th. I have used alot of my time over several days to just get the big datasets imported.


So the way i mesurred time in my test is simply to take the current time in nanoseconds before the start of the request and then measure the time agian after and them get the diffrence.
my code is made in java so here is a simple example:

long nanoTime = System.nanoTime();
sqlCon.createStatement().executeQuery(sql);
nanoTime = System.nanoTime() - nanoTime;

Setup

So my solution is made in java using Netbeans, it is a maven project so you might be able to open it with another IDE.
To setup the databases follow the guides in Helges slides. I'm using mySQL and Neo4J on docker.
To change the config of the connection to the database look in the connector class.

Default setup is as follows:

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


Testing results:

So running the qurries on the full set i got this data:

test results in milisec

|  test         |     MySQL     |              |    Neo4j      |              |
| ------------- |:-------------:|:------------:|:-------------:| ------------:|                             
| Depth         |    median     |   average    |    median     |   average    | 
| ------------- |:-------------:|:------------:|:-------------:| ------------:|
| Depth 1       | 477           |      471     |      61       |       97     | 
| Depth 2       | 17956         |     5584     |      48       |       55     |
| Depth 3       | 311309        |   103596     |      79       |       69     |

So from my results you can clearly see that Neo4J is alot faster for this kind of data. The reason for this is that for the mySQL database each time you do a join it will have to make the cartesian product and then filter out the that that is not needed using the where clause.
For Neo4J the data can be found at a pretty predictial speed since it will only look at the data connected directly with our node and thereforere elimenate alot of lookup that is done in the mySQL database.
