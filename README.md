# Curbee Auto-complete service
The service provides auto-complete suggestions for user supplied search strings against a known dictionary.

### Description
This implementation utilizes an indexing strategy to optimize the performance of locating words in a given dictionary that start with a user supplied string.  To build the index, at initialization time the service predetermines every possible starting sequence in the dictionary up to a given maximum length (10 characters).  It then stores each sequence in a Map along with a Set containing each word beginning with the string.  When a request is received, the service determines if the search string length is less than the maximum and, if so, retrieves the Set associated with the string from the index.  If not, the service shortens the search string to the maximum length, retrieves the Set associated with the shortened string, and then finds all words in this Set containing the full search string. 
 
This two tier strategy fully optimizes response times for more common use cases of search strings up to 10 characters while still providing optimized performance as search strings exceed the threshold.  Testing demonstrates highly consistent, very fast (<10ms generally) query times regardless of search string length up to the threshold.  Query times remain consistent and fast (<20ms generally) as search strings increase above the threshold as well.  (Testing performed on a MacBook Apple M2 w/ 8GB RAM.)

The design of this system leverages the fact that the data set is known in advance which allows us to take advantage of pre-processing strategies to optimize performance.  The service layer within this system is responsible for index management and access.  It is singleton scoped and initialized at startup ensuring that the overhead of creating the index is only incurred once at startup and that there is only one instance of the index in memory.  Runtime performance of the system will remain consistent regardless of data set size.  

Utilizing the Scrabble Words data set as a benchmark, the indexing time averages around 300 ms for a dictionary of ~280K words.  As the size grows we can anticipate increases in the indexing time as well as memory usage - the exact rate will depend on the density of starting sequences.  This system can be scaled vertically and horizontally to accommodate increases in dictionary size and user volume.  Performance on a each instance will remain consistent up to the maximum connections supported by the Spring / Tomcat configuration and underlying infrastructure.  Scaling to thousands or even a million users or more is feasible.

The index is built using a HashMap containing a String based key and a HashSet for the value.  HashMap was chosen due to it's constant O(1) lookup performance which ensures the best performance for locating a starting sequence in the map and retrieving its associations.  HashMap is also thread safe for read operations accommodating support for concurrent users.  HashSet was chosen as the container for words associated with a key to prevent potential duplicates.


**References**
* [Class HashMap<K,V> Java doc](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HashMap.html)
* [HashMap Deep Dive](https://www.linkedin.com/pulse/hashmap-jyoti-jindal)
* [The Fundamentals of the Big-O Notation](https://towardsdatascience.com/the-fundamentals-of-the-big-o-notation-7fe14210b675)



### Installation
Prerequisites: Java 17, Maven 3.9.1 (tested against these versions)

To build the project execute <b>build.sh</b> from within the project directory.

To run the application after building execute <b>run.sh</b> from within the project directory.

The application may be accessed at: http://localhost:8082
* auto-complete end-point: GET [http://localhost:8082/api/curbee/autocomplete/<prefix>](http://localhost:8082/api/curbee/autocomplete/<prefix>)
* rest api documentation: [http://localhost:8082/swagger-ui/index.html?displayRequestDuration=true&operationsSorter=method&configUrl=/api-docs/swagger-config#/](http://localhost:8082/swagger-ui/index.html?displayRequestDuration=true&operationsSorter=method&configUrl=/api-docs/swagger-config#/)


### Improvements & Effort
Overall this implementation was about 4 hours of effort.  The index service and controller implementation took under an hour and the bulk of the effort centered around testing and validating the approach.  Some time was spent at the onset trying an implementation using the KMP algorithm to facilitate a contains matching strategy (match if user supplied value appears anywhere in word) before I realized the specification called for auto-complete which is starts with matching.

Some things I'd consider if I had unlimited cycles:
* allow users to specify max results instead of defaulting to 10
* provide an option to match on words containing the value instead of just beginning with it
* provide details on index byte size to better measure memory usage for different dictionaries
* try caching the index to a file and reading it in at startup to see if startup performance improves
* test this with other dictionaries
* try processing the dictionary in parallel to build the index, perhaps grouped by starting letter, and see if this optimizes indexing for much larger dictionaries


