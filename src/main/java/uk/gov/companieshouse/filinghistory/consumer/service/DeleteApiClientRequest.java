package uk.gov.companieshouse.filinghistory.consumer.service;

public record DeleteApiClientRequest(String transactionId, String companyNumber, String entityId, String deltaAt, String parentEntityId) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String transactionId;
        private String companyNumber;
        private String entityId;
        private String deltaAt;
        private String parentEntityId;

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder parentEntityId(String parentEntityId) {
            this.parentEntityId = parentEntityId;
            return this;
        }

        public DeleteApiClientRequest build() {
            return new DeleteApiClientRequest(transactionId, companyNumber, entityId, deltaAt, parentEntityId);
        }
    }

}
