package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapperArguments;
import uk.gov.companieshouse.filinghistory.consumer.mapper.PreTransformMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerService;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeltaProcessorTest {

    private static final String DELTA_AT = "";
    private static final String CATEGORY = "2";
    private static final String RECEIVE_DATE = "20120804053919";
    private static final String TM01_FORM_TYPE = "TM01";
    private static final String DESCRIPTION = "Appointment Terminated, Director JOHN TESTER";
    private static final String BARCODE = "XAITVXAX";
    private static final String DOCUMENT_ID = "000%s4682".formatted(BARCODE);
    private static final String COMPANY_NUMBER = "12345678";
    private static final String ENTITY_ID = "3056742847";
    private static final String PARENT_ENTITY_ID = "";
    private static final String PARENT_FORM_TYPE = "";
    private static final DescriptionValues DESCRIPTION_VALUES = new DescriptionValues()
            .resignationDate("04/08/2012")
            .OFFICER_NAME("John Tester");
    private static final String PRE_SCANNED_BATCH = "0";

    @InjectMocks
    private FilingHistoryDeltaProcessor mapper;

    @Mock
    private TransactionKindService kindService;
    @Mock
    private PreTransformMapper preTransformMapper;
    @Mock
    private TransformerService transformerService;
    @Mock
    private InternalFilingHistoryApiMapper internalFilingHistoryApiMapper;

    @Mock
    private TransactionKindResult kindResult;
    @Mock
    private ObjectNode preTransformNode;
    @Mock
    private JsonNode postTransformNode;
    @Mock
    private InternalFilingHistoryApi expected;

    @Test
    void shouldProcessDeltaObjectAndReturnMappedInternalFilingHistoryApiObject() {
        // given
        final TransactionKindCriteria criteria = new TransactionKindCriteria(ENTITY_ID, PARENT_ENTITY_ID, TM01_FORM_TYPE, PARENT_FORM_TYPE, BARCODE);
        InternalFilingHistoryApiMapperArguments expectedArguments = new InternalFilingHistoryApiMapperArguments(
                postTransformNode,
                kindResult,
                COMPANY_NUMBER,
                DELTA_AT,
                "contextId");
        final FilingHistoryDelta delta = buildFilingHistoryDelta();

        when(kindService.encodeIdByTransactionKind(any())).thenReturn(kindResult);
        when(preTransformMapper.mapDeltaToObjectNode(any())).thenReturn(preTransformNode);
        when(transformerService.transform(any())).thenReturn(postTransformNode);
        when(internalFilingHistoryApiMapper.mapJsonNodeToInternalFilingHistoryApi(any())).thenReturn(expected);

        // when
        final InternalFilingHistoryApi actual = mapper.processDelta(delta, "contextId");

        // then
        assertEquals(expected, actual);
        verify(kindService).encodeIdByTransactionKind(criteria);
        verify(preTransformMapper).mapDeltaToObjectNode(delta.getFilingHistory().getFirst());
        verify(transformerService).transform(preTransformNode);
        verify(internalFilingHistoryApiMapper).mapJsonNodeToInternalFilingHistoryApi(expectedArguments);
    }

    private static FilingHistoryDelta buildFilingHistoryDelta() {
        return new FilingHistoryDelta()
                .filingHistory(List.of(new FilingHistory()
                        .category(CATEGORY)
                        .receiveDate(RECEIVE_DATE)
                        .formType(TM01_FORM_TYPE)
                        .description(DESCRIPTION)
                        .barcode(BARCODE)
                        .documentId(DOCUMENT_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .entityId(ENTITY_ID)
                        .parentEntityId(PARENT_ENTITY_ID)
                        .parentFormType(PARENT_FORM_TYPE)
                        .descriptionValues(DESCRIPTION_VALUES)
                        .preScannedBatch(PRE_SCANNED_BATCH)))
                .deltaAt(DELTA_AT);
    }
}