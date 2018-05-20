package com.saver.file;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.http.MediaType;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@RunWith(BlockJUnit4ClassRunner.class)
public class IntegrationTest {
    private String TEST_DIR = new File(getClass().getClassLoader().getResource("files").getPath()).getAbsolutePath();

    private String NODE_ID_1 = "5cc06d4d-b7a3-4b28-a901-3c3f50e421a6";
    private String NODE_ID_2 = "19b1bc0c-85da-4323-b94c-75c40fac5165";
    private String NODE_ID_3 = "bf125822-1918-4448-bcb7-68b0164891f9";

    private static final String DOCKER_COMPOSE_TEST = "deploy/docker-compose.yml";

    private static DockerComposeContainer container = new DockerComposeContainer(new File(DOCKER_COMPOSE_TEST))
            .withTailChildContainers(true)
            .withLocalCompose(true);

    static {
        container.starting(Description.EMPTY);
    }

    @Before
    public void prepare() {
        File dir = new File("deploy/storage");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    @Test
    public void fullTest() throws InterruptedException, IOException {
        Thread.sleep(10000);
        File file = new File(TEST_DIR, "text.txt");

        UUID key = given()
                .multiPart("file", file, MediaType.TEXT_PLAIN_VALUE)
                .when()
                .post("http://localhost:80/node-1/file-api/upload")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(UUID.class);

        given()
                .when()
                .get("http://localhost:80/node-1/file-api/" + key + "/download")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(new String(Files.readAllBytes(file.toPath()))));

        given()
                .when()
                .get("http://localhost:80/node-2/file-api/" + key + "/download")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);


        given()
                .when()
                .get("http://localhost:80/node-3/file-api/" + key + "/download")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        File fileNode1 = new File("deploy/storage/file/" + NODE_ID_1 + "/" + key);
        File fileNode2 = new File("deploy/storage/file/" + NODE_ID_2 + "/" + key);
        File fileNode3 = new File("deploy/storage/file/" + NODE_ID_3 + "/" + key);

        Assert.assertFalse(fileNode1.exists());
        Assert.assertTrue(fileNode2.exists() || fileNode3.exists());
    }

}
