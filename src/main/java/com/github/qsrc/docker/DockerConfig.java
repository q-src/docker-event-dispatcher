package com.github.qsrc.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClient createDockerClient() {
        return DockerClientBuilder.getInstance().build();
    }

}
