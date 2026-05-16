package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackListResponse
import com.devscion.auditforge.domain.model.PolicyPackSummary
import com.devscion.auditforge.domain.model.PolicyRule
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.data.network.ApiResult
import kotlinx.coroutines.delay

class MockPolicyRepository : PolicyRepository {

    private val packs = listOf(
        PolicyPackSummary(
            id = "owasp-api-top10-2023",
            name = "OWASP API Top 10 (2023)",
            framework = PolicyFramework.OWASP,
            version = "2023",
            description = "Industry-standard API security risks covering authentication, authorization, and injection flaws.",
            ruleCount = 10,
            isCustom = false,
        ),
        PolicyPackSummary(
            id = "hipaa-technical-safeguards",
            name = "HIPAA Technical Safeguards",
            framework = PolicyFramework.HIPAA,
            version = "2024-01",
            description = "164.312 technical safeguards for protecting electronic protected health information.",
            ruleCount = 18,
            isCustom = false,
        ),
        PolicyPackSummary(
            id = "soc2-cc-series",
            name = "SOC 2 Common Criteria",
            framework = PolicyFramework.SOC2,
            version = "2022",
            description = "AICPA Trust Services Criteria for security, availability, and confidentiality.",
            ruleCount = 31,
            isCustom = false,
        ),
        PolicyPackSummary(
            id = "pci-dss-v4",
            name = "PCI DSS v4.0",
            framework = PolicyFramework.PCI_DSS,
            version = "4.0",
            description = "Payment Card Industry Data Security Standard for cardholder data environments.",
            ruleCount = 27,
            isCustom = false,
        ),
        PolicyPackSummary(
            id = "gdpr-technical",
            name = "GDPR Technical Controls",
            framework = PolicyFramework.GDPR,
            version = "2018",
            description = "GDPR Article 25 and 32 technical requirements for data protection by design.",
            ruleCount = 15,
            isCustom = false,
        ),
    )

    private val packDetails = mapOf(
        "owasp-api-top10-2023" to PolicyPackDetail(
            id = "owasp-api-top10-2023",
            name = "OWASP API Top 10 (2023)",
            framework = PolicyFramework.OWASP,
            version = "2023",
            description = "Industry-standard API security risks.",
            rules = listOf(
                PolicyRule(
                    "owasp-api1",
                    "Broken Object Level Authorization",
                    "Check for missing object-level auth on endpoints.",
                    RuleSeverity.Critical,
                    "authorization",
                    "ai_evaluation",
                    listOf("OWASP-API1:2023")
                ),
                PolicyRule(
                    "owasp-api2",
                    "Broken Authentication",
                    "Detect endpoints missing authentication requirements.",
                    RuleSeverity.Critical,
                    "authentication",
                    "schema_validation",
                    listOf("OWASP-API2:2023")
                ),
                PolicyRule(
                    "owasp-api3",
                    "Broken Object Property Level Authorization",
                    "Detect over-exposed object properties.",
                    RuleSeverity.High,
                    "authorization",
                    "ai_evaluation",
                    listOf("OWASP-API3:2023")
                ),
                PolicyRule(
                    "owasp-api4",
                    "Unrestricted Resource Consumption",
                    "Check for missing rate limits and pagination.",
                    RuleSeverity.Medium,
                    "configuration",
                    "schema_validation",
                    listOf("OWASP-API4:2023")
                ),
                PolicyRule(
                    "owasp-api5",
                    "Broken Function Level Authorization",
                    "Detect admin endpoints accessible to regular users.",
                    RuleSeverity.Critical,
                    "authorization",
                    "ai_evaluation",
                    listOf("OWASP-API5:2023")
                ),
                PolicyRule(
                    "owasp-api6",
                    "Unrestricted Access to Sensitive Business Flows",
                    "Detect business logic flows missing abuse controls.",
                    RuleSeverity.High,
                    "authentication",
                    "ai_evaluation",
                    listOf("OWASP-API6:2023")
                ),
                PolicyRule(
                    "owasp-api7",
                    "Server Side Request Forgery",
                    "Detect SSRF vulnerabilities in URL parameters.",
                    RuleSeverity.High,
                    "input_validation",
                    "pattern",
                    listOf("OWASP-API7:2023")
                ),
                PolicyRule(
                    "owasp-api8",
                    "Security Misconfiguration",
                    "Detect insecure defaults and debug endpoints.",
                    RuleSeverity.Medium,
                    "configuration",
                    "schema_validation",
                    listOf("OWASP-API8:2023")
                ),
                PolicyRule(
                    "owasp-api9",
                    "Improper Inventory Management",
                    "Detect undocumented or deprecated endpoints.",
                    RuleSeverity.Low,
                    "configuration",
                    "schema_validation",
                    listOf("OWASP-API9:2023")
                ),
                PolicyRule(
                    "owasp-api10",
                    "Unsafe Consumption of APIs",
                    "Detect insecure third-party API integrations.",
                    RuleSeverity.Medium,
                    "authentication",
                    "ai_evaluation",
                    listOf("OWASP-API10:2023")
                ),
            ),
        ),
        "hipaa-technical-safeguards" to PolicyPackDetail(
            id = "hipaa-technical-safeguards",
            name = "HIPAA Technical Safeguards",
            framework = PolicyFramework.HIPAA,
            version = "2024-01",
            description = "164.312 technical safeguards for PHI.",
            rules = listOf(
                PolicyRule(
                    "hipaa-164312a1",
                    "Access Control",
                    "Unique user identification and emergency access procedures.",
                    RuleSeverity.Critical,
                    "authentication",
                    "ai_evaluation",
                    listOf("HIPAA-164.312(a)(1)")
                ),
                PolicyRule(
                    "hipaa-164312a2i",
                    "Unique User Identification",
                    "Each user must have a unique identifier.",
                    RuleSeverity.High,
                    "authentication",
                    "schema_validation",
                    listOf("HIPAA-164.312(a)(2)(i)")
                ),
                PolicyRule(
                    "hipaa-164312b",
                    "Audit Controls",
                    "Hardware and software activity logs must be recorded.",
                    RuleSeverity.High,
                    "logging",
                    "ai_evaluation",
                    listOf("HIPAA-164.312(b)")
                ),
                PolicyRule(
                    "hipaa-164312c1",
                    "Integrity Controls",
                    "Protect PHI from improper alteration or destruction.",
                    RuleSeverity.High,
                    "encryption",
                    "ai_evaluation",
                    listOf("HIPAA-164.312(c)(1)")
                ),
                PolicyRule(
                    "hipaa-164312e1",
                    "Transmission Security",
                    "PHI transmitted over networks must be encrypted.",
                    RuleSeverity.Critical,
                    "encryption",
                    "schema_validation",
                    listOf("HIPAA-164.312(e)(1)")
                ),
            ),
        ),
    )

    override suspend fun getPolicyPacks(framework: PolicyFramework?): ApiResult<PolicyPackListResponse> {
        delay(300)
        val filtered = if (framework == null) packs else packs.filter { it.framework == framework }
        return ApiResult.Success(PolicyPackListResponse(data = filtered))
    }

    override suspend fun getPolicyPackDetail(packId: String): ApiResult<PolicyPackDetail> {
        delay(200)
        val detail = packDetails[packId]
        return if (detail != null) ApiResult.Success(detail)
        else ApiResult.Error("Policy pack not found", 404)
    }
}
