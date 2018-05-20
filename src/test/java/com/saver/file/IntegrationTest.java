package com.saver.file;

import org.apache.http.HttpStatus;
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

    private static final String DOCKER_COMPOSE_TEST = "deploy/docker-compose.yml";

    private static DockerComposeContainer container = new DockerComposeContainer(new File(DOCKER_COMPOSE_TEST))
            .withTailChildContainers(true)
            .withLocalCompose(true);

    static {
        container.starting(Description.EMPTY);
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
    }

}
