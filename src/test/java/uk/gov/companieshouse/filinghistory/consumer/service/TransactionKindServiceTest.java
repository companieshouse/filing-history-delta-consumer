package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@ExtendWith(MockitoExtension.class)
class TransactionKindServiceTest {

    private static final String ENTITY_ID = "entityId";
    private static final String PARENT_ENTITY_ID = "parentEntityId";
    private static final String BARCODE = "barcode";
    private static final String SALT = "salt";
    private static final String ENCODED_ENTITY_ID =
            Base64.encodeBase64URLSafeString((trim(ENTITY_ID) + SALT).getBytes(StandardCharsets.UTF_8));
    private static final String ENCODED_PARENT_ENTITY_ID =
            Base64.encodeBase64URLSafeString((trim(PARENT_ENTITY_ID) + SALT).getBytes(StandardCharsets.UTF_8));
    private static final String ENCODED_BARCODE =
            Base64.encodeBase64URLSafeString((trim(BARCODE) + SALT).getBytes(StandardCharsets.UTF_8));

    @InjectMocks
    private TransactionKindService kindService;

    @Mock
    private FormTypeService formTypeService;

    @BeforeEach
    void setUp() {
        kindService = new TransactionKindService(formTypeService, SALT);
    }

    @Test
    void shouldEncodeEntityId() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "TM01",
                "",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldEncodeParentEntityIdWhenChildAnnotation() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                PARENT_ENTITY_ID,
                "ANNOTATION",
                "ANY",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_PARENT_ENTITY_ID,
                TransactionKindEnum.ANNOTATION);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldEncodeEntityIdWhenTopLevelAnnotation() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "ANNOTATION",
                "",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.ANNOTATION);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldEncodeBarcodeWhenResolution() {
        // given
        when(formTypeService.isResolutionType(any())).thenReturn(true);

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "RES01",
                "",
                BARCODE);
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_BARCODE,
                TransactionKindEnum.RESOLUTION);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isResolutionType("RES01");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''", "null"
    }, nullValues = {"null"})
    void shouldEncodeEntityIdWhenResolutionButBarcodeBlank(String barcode) {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "RES01",
                "",
                barcode);
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isAssociatedFilingBlockListed(criteria);
        verifyNoMoreInteractions(formTypeService);
    }

    @Test
    void shouldEncodeEntityIdWhenRES15() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "RES15",
                "",
                BARCODE);
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isAssociatedFilingBlockListed(criteria);
        verifyNoMoreInteractions(formTypeService);
    }

    @Test
    void shouldEncodeParentEntityIdWhenAssociatedFiling() {
        // given
        when(formTypeService.isAssociatedFilingBlockListed(any())).thenReturn(false);

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                PARENT_ENTITY_ID,
                "any",
                "any",
                BARCODE);
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_PARENT_ENTITY_ID,
                TransactionKindEnum.ASSOCIATED_FILING);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isAssociatedFilingBlockListed(criteria);
    }

    @Test
    void shouldEncodeEntityIdWhenAssociatedFilingBlockListed() {
        // given
        when(formTypeService.isAssociatedFilingBlockListed(any())).thenReturn(true);

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "any",
                "any",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isAssociatedFilingBlockListed(criteria);
    }

    @Test
    void shouldEncodeEntityIdWhenAssociatedFilingNotBlockListedButMissingParentEntityId() {
        // given
        when(formTypeService.isAssociatedFilingBlockListed(any())).thenReturn(false);

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                ENTITY_ID,
                "",
                "any",
                "any",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                ENCODED_ENTITY_ID,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
        verify(formTypeService).isAssociatedFilingBlockListed(criteria);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "''"
    },
            nullValues = {"null"})
    void shouldReturnUnchangedIdIfNullOrEmpty(final String entityId) {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                entityId,
                "",
                "TM01",
                "",
                "");
        TransactionKindResult expected = new TransactionKindResult(
                entityId,
                TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }
}