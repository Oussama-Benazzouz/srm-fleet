package ma.srm.fleet.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUploadTooLarge_retourne413() {
        ProblemDetail pd = handler.handleUploadTooLarge(new MaxUploadSizeExceededException(1_048_576L));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE.value());
    }
}
