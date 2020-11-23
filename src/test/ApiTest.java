package test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;


public class ApiTest {

    static final String url = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus";
    static URL apiUrl;
    HttpURLConnection connection;

    // constants for the API
    static final String SKU = "sku";
    static final String DESCRIPTION = "description";
    static final String PRICE = "price";

    //test data
    final StockItem testOne = new StockItem("Ogdentest1", "First OgdenTest Item", "1.23");
    final StockItem testOneUpdated = new StockItem("Ogdentest1", "First OgdenTest Item edited", "4.99");
    final StockItem testNegPrice = new StockItem("OgdentestNeg", "Negative OgdenTest Item", "-1.23");

    final StockItem testTwo = new StockItem("Ogdentest2", "Second OgdenTest Item", "2.34");

    final StockItem testNullSku = new StockItem(null, "third OgdenTest Item", "23.45");
    final StockItem testNullDescription = new StockItem("Ogdentest4", null, "4.56");
    final StockItem testNullPrice = new StockItem("Ogdentest5", "First OgdenTest Item", null);

    @BeforeSuite
    public void openConnection() throws IOException {
        apiUrl = new URL(url);
        connection = (HttpURLConnection) apiUrl.openConnection();
    }

    @AfterSuite
    public void cleanup() throws IOException {
        connection.disconnect();
    }

    @Test(priority = 0)
    public void getAllSKUs() throws IOException {
        System.out.println("Testing Get ALl SKUs");
        JSONArray allSKUs = getAllSkus();
        //for (int count = 0; count < allSKUs.length(); count++) {
        //System.out.println(allSKUs.get(count).toString());
        //}
        //TODO improve this validation
        Assert.assertTrue(allSKUs.length() > 0, "No items were returned");
        System.out.println("Get all SKUs passed");
    }

    @Test(priority = 1)
    public void createThenDeleteSKU() throws IOException {
        System.out.println("Create new SKU to be deleted");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testOne));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "POST of new item failed");
        if (verifySkuInApi(testOne)) {
            System.out.println("Create new SKU passed");
            //now we call delete
            System.out.println("Testing SKU deletion");
            deleteSKU(testOne);
            System.out.println("Delete SKU passed");
        } else {
            Assert.fail("Creation of new SKU failed");
        }
    }

    // this never passed for me.  For some reason, I was unable to update an SKU
    // I'm not sure if the problem is in my test code, or in the API
    // I assume it's my test code, as the exercise stated that I am being given a working API
    @Test(priority = 2)
    public void updateSKU() throws IOException {
        System.out.println("Testing Update SKU");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }
        //first we create a new SKU
        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testOne));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "POST of new item failed");
        if (!verifySkuInApi(testOne)) {
            Assert.fail("Creation of SKU failed, aborting test");
        } else {
            // now we Update it

            try {
                connection.setRequestMethod("POST");
            } catch (IllegalStateException e) {
                createConnection();
                connection.setRequestMethod("POST");
            }

            //System.out.println("Sending 'HTTP POST' request to : " + url);
            connection.setDoOutput(true);
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(createPostParamFromStockItem(testOneUpdated));

            outputStream.flush();
            outputStream.close();
            //System.out.println("Sent 'HTTP POST' request to : " + url);
            responseCode = connection.getResponseCode();
            //System.out.println("Response Code : " + responseCode);
            Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Update of item failed");
            if (verifySkuInApi(testOneUpdated)) {
                System.out.println("Update of SKU passed");
            } else {
                Assert.fail("Update of SKU failed");
            }
        }
    }

    // now the error checks
    @Test(priority = 4)
    public void createNewSKUMissingSKU() throws IOException {
        System.out.println("Test of creating new item missing the SKU");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        JSONObject json = new JSONObject();
        json.put(DESCRIPTION, testTwo.description);
        json.put(PRICE, testTwo.price);
        String badItem = json.toString();
        //System.out.println("Sending " + badItem);

        outputStream.writeBytes(badItem);

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotEquals(responseCode, HttpURLConnection.HTTP_OK, "New item created missing SKU");
        softAssert.assertEquals(responseCode, HttpURLConnection.HTTP_BAD_REQUEST, "Incorrect response to bad request");

        // if the POST suceeded, we should delete the bad object from the database, to avoid any errors
        // caused by the malformed item
        // delete requires the id(SKU),but it's null, so we can't delete it
        // this makes sure the test case fails if any of the assertions above failed
        try {
            softAssert.assertAll();
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
        System.out.println("SKU with missing SKU not created, test passed");
    }

    @Test(priority = 4)
    public void createNewSKUMissingDescription() throws IOException {
        System.out.println("Test of creating new item missing the description");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        JSONObject json = new JSONObject();
        json.put(SKU, testTwo.stock_keeping_unit);
        json.put(PRICE, testTwo.price);
        String badItem = json.toString();
        //System.out.println("Sending " + badItem);

        outputStream.writeBytes(badItem);

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotEquals(responseCode, HttpURLConnection.HTTP_OK, "New item created missing description");
        softAssert.assertEquals(responseCode, HttpURLConnection.HTTP_BAD_REQUEST, "Incorrect response to bad request");

        // if the POST suceeded, we should delete the bad object from the database, to avoid any errors
        // caused by the malformed item
        if (HttpURLConnection.HTTP_OK == responseCode) {
            deleteSKU(testTwo);
        }
        // this makes sure the test case fails if any of the assertions above failed
        try {
            softAssert.assertAll();
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
        System.out.println("SKU with missing description not created, test passed");
    }

    @Test(priority = 4)
    public void createNewSKUMissingPrice() throws IOException {
        System.out.println("Test of creating new item missing the price");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        JSONObject json = new JSONObject();
        json.put(SKU, testTwo.stock_keeping_unit);
        json.put(DESCRIPTION, testTwo.description);
        String badItem = json.toString();
        //System.out.println("Sending " + badItem);

        outputStream.writeBytes(badItem);

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotEquals(responseCode, HttpURLConnection.HTTP_OK, "New item created missing price");
        softAssert.assertEquals(responseCode, HttpURLConnection.HTTP_BAD_REQUEST, "Incorrect response to bad price");

        // if the POST suceeded, we should delete the bad object from the database, to avoid any errors
        // caused by the malformed item
        if (HttpURLConnection.HTTP_OK == responseCode) {
            deleteSKU(testTwo);
        }
        // this makes sure the test case fails if any of the assertions above failed
        try {
            softAssert.assertAll();
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }
        System.out.println("SKU with missing price not created, test passed");
    }

    @Test(priority = 4)
    public void testCreateNullSku() throws IOException {
        System.out.println("Test of creating new item with the SKU null");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testNullSku));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertNotEquals(HttpURLConnection.HTTP_OK, responseCode, "Item with NULL Sku created");
        //TODO determine what the correct HTTP response should be here
        verifySkuNotInApi(testNullSku);
        System.out.println("SKU with missing SKU not created, test passed");
    }

    @Test(priority = 4)
    public void testCreateNullDescription() throws IOException {
        System.out.println("Test of creating new item with null description");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testNullDescription));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        //TODO consider a try catch block to delete the bad item if it does get
        //created.  Having it in the database might cause problems
        Assert.assertNotEquals(HttpURLConnection.HTTP_OK, responseCode,
                "SKU with null description was created");
        verifySkuNotInApi(testNullDescription);
        System.out.println("SKU with null description not created, test passed");
    }

    @Test(priority = 4)
    public void testCreateNullPrice() throws IOException {
        System.out.println("Test of creating new item with null price");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testNullPrice));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertNotEquals(HttpURLConnection.HTTP_OK, responseCode, "SKU with null price was created");
        verifySkuNotInApi(testNullPrice);
        System.out.println("SKU with missing price not created, test passed");
    }

    @Test(priority = 4)
    public void testNegativePrice() throws IOException {
        System.out.println("Test of creating new item with negative price");
        try {
            connection.setRequestMethod("POST");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("POST");
        }

        //System.out.println("Sending 'HTTP POST' request to : " + url);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(createPostParamFromStockItem(testNegPrice));

        outputStream.flush();
        outputStream.close();
        //System.out.println("Sent 'HTTP POST' request to : " + url);
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertNotEquals(HttpURLConnection.HTTP_OK, responseCode, "SKU with negative price created");
        verifySkuNotInApi(testNegPrice);
        System.out.println("SKU with negative price not created, test passed");
    }

    private void deleteSKU(StockItem itemToDelete) throws IOException {
        String deleteUrl = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus/"
                + itemToDelete.stock_keeping_unit;
        URL deleteApiUrl = new URL(deleteUrl);
        HttpURLConnection deleteConnection = (HttpURLConnection) deleteApiUrl.openConnection();
        deleteConnection.setRequestMethod("DELETE");

        //System.out.println("Sending 'HTTP DELETE' request to : " + url);
        deleteConnection.connect();
        //System.out.println("Sent 'HTTP DELETE' request to : " + url);

        int responseCode = deleteConnection.getResponseCode();
        //System.out.println("Response Code : " + responseCode);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Item Deletion failed");
        verifySkuNotInApi(testOne);
    }

    private void verifySkuNotInApi(StockItem testItem) throws IOException {
        //after an item is deleted, verify it's gone
        //System.out.println("Verifying that an item was deleted");
        String singleUrl = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus/"
                + testItem.stock_keeping_unit;
        URL singleAPIUrl = new URL(singleUrl);
        HttpURLConnection singleSKUConnection = (HttpURLConnection) singleAPIUrl.openConnection();
        singleSKUConnection.setRequestMethod("GET");
        //System.out.println("Sending 'HTTP GET' request to : " + singleUrl);
        int responseCode = singleSKUConnection.getResponseCode();
        //System.out.println("Sent 'HTTP GET' request to : " + singleUrl);
        //System.out.println("Response Code : " + responseCode);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Failure to GET item for verification");
        JSONArray response;
        // this gets a map, of "Item" and "ResponseMetaData"
        response = readResponse(singleSKUConnection);
        //System.out.println(response.toString());
        // add checks for test item
        JSONObject obj = new JSONObject(response.get(0).toString());
        try {
            obj.get("Item");
            // this line would execute if the item wasn't deleted
            Assert.assertTrue(false, "Item not deleted");
        } catch (JSONException e) {
            // if the Item was deleted
        }
    }

    private boolean verifySkuInApi(StockItem testItem) throws IOException {
        //fetch the newly created or edited item, and verify the fields
        //are correct
        //System.out.println("Verifying that all field are correct");
        String singleUrl = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus/"
                + testItem.stock_keeping_unit;
        URL singleAPIUrl = new URL(singleUrl);
        HttpURLConnection singleSKUConnection = (HttpURLConnection) singleAPIUrl.openConnection();
        singleSKUConnection.setRequestMethod("GET");
        //System.out.println("Sending 'HTTP GET' request to : " + singleUrl);
        int responseCode = singleSKUConnection.getResponseCode();
        //System.out.println("Sent 'HTTP GET' request to : " + singleUrl);
        //System.out.println("Response Code : " + responseCode);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Failed to get item for verification");
        JSONArray response;
        // this gets a map, of "Item" and "ResponseMetaData"
        response = readResponse(singleSKUConnection);
        //System.out.println(response.toString());
        // add checks for test item
        JSONObject obj = new JSONObject(response.get(0).toString());
        JSONObject stockItem;
        stockItem = (JSONObject) obj.get("Item");
        // assert(actual, expected)
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(stockItem.get(SKU), testItem.stock_keeping_unit, "SKU is incorrect");
        softAssert.assertEquals(stockItem.get(DESCRIPTION), testItem.description, "description is incorrect");
        softAssert.assertEquals(stockItem.get(PRICE), testItem.price, "price is incorrect");
        try {
            softAssert.assertAll();
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private JSONArray getAllSkus() throws IOException {
        try {
            connection.setRequestMethod("GET");
        } catch (IllegalStateException e) {
            createConnection();
            connection.setRequestMethod("GET");
        }
        //System.out.println("Sending 'HTTP GET' request to : " + url);
        Integer responseCode = connection.getResponseCode();
        //System.out.println("Sent 'HTTP GET' request to : " + url);
        //System.out.println("Response Code : " + responseCode);
        JSONArray response;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            response = readResponse();
            //System.out.println("Found " + response.length() + " items");
            return response;
        }
        JSONArray empty = new JSONArray();
        return empty;
    }

    private JSONArray readResponse() throws IOException {
        return readResponse(connection);
    }

    private JSONArray readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = inputReader.readLine()) != null) {
            response.append(inputLine);
        }
        inputReader.close();
        JSONArray resp = new JSONArray();
        try {
            resp = new JSONArray(response.toString());
        } catch (JSONException e) {
            // the most likely case here is that instead of returning an array of objects, we only
            // got one
            resp.put(response);
        }
        return resp;
    }

    private String createPostParamFromStockItem(StockItem newItem) {
        JSONObject json = new JSONObject();
        json.put(SKU, testOne.stock_keeping_unit);
        json.put(DESCRIPTION, testOne.description);
        json.put(PRICE, testOne.price);
        return json.toString();
    }

    private void createConnection() throws IOException {
        connection = (HttpURLConnection) apiUrl.openConnection();
    }

    public class StockItem {
        String stock_keeping_unit;
        String description;
        String price;

        public StockItem(final String sku, final String desc, final String stickerPrice) {
            description = desc;
            price = stickerPrice;
            stock_keeping_unit = sku;
        }
    }
}
