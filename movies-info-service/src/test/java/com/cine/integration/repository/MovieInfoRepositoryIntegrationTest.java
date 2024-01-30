package com.cine.integration.repository;

import com.cine.domain.MovieInfo;
import com.cine.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
public class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movie1 = new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var movie2 = new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));
        var movie3 = new MovieInfo("abc", "The Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        movieInfoRepository.saveAll(List.of(movie1, movie2, movie3))
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {

        // when
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll();

        // then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {

        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc");

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertThat(movieInfo.getName()).isEqualTo("The Dark Knight Rises");
                    assertThat(movieInfo.getYear()).isEqualTo(2012) ;
                    assertThat(movieInfo.getReleaseDate()).isEqualTo(LocalDate.parse("2012-07-20")) ;
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        // given
        var movieInfo = new MovieInfo(null, "Batman Begins Sample", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo);

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(result -> {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getName()).isEqualTo(movieInfo.getName());
                    assertThat(result.getYear()).isEqualTo(movieInfo.getYear()) ;
                    assertThat(result.getReleaseDate()).isEqualTo(movieInfo.getReleaseDate()) ;
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        // given
        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2013);

        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo);

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(result -> assertThat(result.getYear()).isEqualTo(movieInfo.getYear()))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        // given
        String movieInfoId = "abc";

        // when
        movieInfoRepository.deleteById(movieInfoId).block();

        var moviesInfoFlux = movieInfoRepository.findAll();

        // then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}
