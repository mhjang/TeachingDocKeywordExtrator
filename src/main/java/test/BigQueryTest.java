package test;

import com.google.cloud.bigquery.samples.BigQuerySender;

/**
 * Created by mhjang on 2/21/14.
 */
public class BigQueryTest {
    public static void main(String[] args) {
        BigQuerySender bq = new BigQuerySender();
        System.out.println(bq.googleContainsTrigram("minimum spanning tree"));
        System.out.println(bq.googleContainsTrigram("basic data structure"));
        System.out.println(bq.googleContainsTrigram("divide and conquer"));

    }
}
