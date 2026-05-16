package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingEvidence
import com.devscion.auditforge.domain.model.FindingListResponse
import com.devscion.auditforge.domain.model.FindingRemediation
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.PaginationMeta
import com.devscion.auditforge.domain.model.PolicyCoverage
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.ScoreBreakdown
import com.devscion.auditforge.domain.model.SeverityBreakdown
import com.devscion.auditforge.domain.model.StatusBreakdown
import com.devscion.auditforge.domain.model.UpdateFindingStatusRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MockFindingsRepository : FindingsRepository {

    private val mutex = Mutex()
    private val findings: MutableMap<String, MutableList<Finding>> = mutableMapOf()

    override suspend fun getFindings(
        sessionId: String,
        severity: Set<RuleSeverity>,
        status: FindingStatus?,
        category: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<FindingListResponse> {
        delay(350)
        return mutex.withLock {
            val all = getOrSeedFindings(sessionId)
            val filtered = all
                .filter { severity.isEmpty() || it.severity in severity }
                .filter { status == null || it.status == status }
                .filter { category == null || it.category.equals(category, ignoreCase = true) }
            val total = filtered.size
            val totalPages = maxOf(1, (total + pageSize - 1) / pageSize)
            val pageItems = filtered.drop((page - 1) * pageSize).take(pageSize)
            ApiResult.Success(
                FindingListResponse(
                    data = pageItems,
                    pagination = PaginationMeta(
                        page = page,
                        pageSize = pageSize,
                        totalItems = total,
                        totalPages = totalPages,
                    ),
                )
            )
        }
    }

    override suspend fun getFinding(sessionId: String, findingId: String): ApiResult<Finding> {
        delay(150)
        return mutex.withLock {
            val finding = getOrSeedFindings(sessionId).find { it.id == findingId }
            if (finding != null) ApiResult.Success(finding)
            else ApiResult.Error("Finding not found", 404)
        }
    }

    override suspend fun updateFindingStatus(
        sessionId: String,
        findingId: String,
        request: UpdateFindingStatusRequest,
    ): ApiResult<Finding> {
        delay(250)
        return mutex.withLock {
            val list = getOrSeedFindings(sessionId)
            val index = list.indexOfFirst { it.id == findingId }
            if (index < 0) return@withLock ApiResult.Error("Finding not found", 404)
            val updated = list[index].copy(
                status = request.status,
                comment = request.comment,
                updatedAt = MOCK_TIMESTAMP,
            )
            list[index] = updated
            ApiResult.Success(updated)
        }
    }

    override suspend fun getFindingsSummary(sessionId: String): ApiResult<FindingsSummary> {
        delay(200)
        return mutex.withLock {
            val all = getOrSeedFindings(sessionId)
            val criticalCount = all.count { it.severity == RuleSeverity.Critical }
            val highCount = all.count { it.severity == RuleSeverity.High }
            val mediumCount = all.count { it.severity == RuleSeverity.Medium }
            val lowCount = all.count { it.severity == RuleSeverity.Low }
            val openCount = all.count { it.status == FindingStatus.Open }
            val ackCount = all.count { it.status == FindingStatus.Acknowledged }
            val fpCount = all.count { it.status == FindingStatus.FalsePositive }
            val resolvedCount = all.count { it.status == FindingStatus.Resolved }
            val byCategory = all.groupBy { it.category }.mapValues { it.value.size }
            ApiResult.Success(
                FindingsSummary(
                    sessionId = sessionId,
                    scanId = "mock-scan-id",
                    totalFindings = all.size,
                    bySeverity = SeverityBreakdown(criticalCount, highCount, mediumCount, lowCount),
                    byStatus = StatusBreakdown(openCount, ackCount, fpCount, resolvedCount),
                    byCategory = byCategory,
                    overallScore = 76,
                    scoreBreakdown = ScoreBreakdown(rulesPassed = 47, rulesFailed = all.size, rulesTotal = 47 + all.size),
                    policyCoverage = listOf(
                        PolicyCoverage("owasp-api-top10-2023", 23, 18, 5),
                        PolicyCoverage("hipaa-technical-safeguards", 18, 16, 2),
                    ),
                )
            )
        }
    }

    private fun getOrSeedFindings(sessionId: String): MutableList<Finding> =
        findings.getOrPut(sessionId) { buildMockFindings(sessionId).toMutableList() }

    private fun buildMockFindings(sessionId: String): List<Finding> = listOf(
        Finding(
            id = "f-001",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api2-broken-auth",
            ruleName = "Broken Authentication",
            severity = RuleSeverity.Critical,
            category = "authentication",
            title = "API endpoint missing authentication",
            description = "The endpoint POST /api/v1/users/{id}/delete does not require authentication, allowing any caller to delete user records without proving their identity.",
            evidence = FindingEvidence(
                sourceType = "openapi_spec",
                sourcePath = "patient-portal-api.yaml",
                lineNumber = 142,
                snippet = "delete:\n  summary: Delete user\n  responses:\n    '204':\n      description: Deleted",
                context = "No 'security' key present on this endpoint",
            ),
            remediation = FindingRemediation(
                summary = "Add an authentication requirement to this endpoint using the BearerAuth security scheme.",
                codeExample = "delete:\n  summary: Delete user\n  security:\n    - BearerAuth: []\n  responses:\n    '204':\n      description: Deleted",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa2-broken-authentication/"),
            ),
            complianceRefs = listOf("OWASP-API2:2023", "HIPAA-164.312(a)(1)"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-002",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api8-security-misconfiguration",
            ruleName = "Security Misconfiguration",
            severity = RuleSeverity.High,
            category = "configuration",
            title = "Debug mode enabled in production config",
            description = "The application configuration has DEBUG=True set, which exposes detailed error tracebacks and internal state to any client that triggers an error response.",
            evidence = FindingEvidence(
                sourceType = "config",
                sourcePath = "config/production.env",
                lineNumber = 12,
                snippet = "DEBUG=True\nSECRET_KEY=dev-key-change-me",
                context = "Debug mode must be disabled in production environments",
            ),
            remediation = FindingRemediation(
                summary = "Set DEBUG=False and ensure SECRET_KEY is a cryptographically random value unique to production.",
                codeExample = "DEBUG=False\nSECRET_KEY=\${SECRET_KEY_FROM_VAULT}",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa8-security-misconfiguration/"),
            ),
            complianceRefs = listOf("OWASP-API8:2023", "SOC2-CC6.1"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-003",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api1-broken-object-auth",
            ruleName = "Broken Object Level Authorization",
            severity = RuleSeverity.Critical,
            category = "authorization",
            title = "Broken object-level authorization in user endpoint",
            description = "The GET /api/v1/users/{user_id} endpoint returns user data for any user_id without verifying that the caller has authorization to view that specific user's data.",
            evidence = FindingEvidence(
                sourceType = "openapi_spec",
                sourcePath = "patient-portal-api.yaml",
                lineNumber = 87,
                snippet = "get:\n  summary: Get user by ID\n  parameters:\n    - name: user_id\n      in: path\n      required: true",
                context = "No ownership or role check documented for this endpoint",
            ),
            remediation = FindingRemediation(
                summary = "Verify that the authenticated user owns the resource or has an explicit admin grant before returning data.",
                codeExample = "if request.user.id != user_id and not request.user.is_admin:\n    raise PermissionDenied()",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/"),
            ),
            complianceRefs = listOf("OWASP-API1:2023", "HIPAA-164.312(a)(1)"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-004",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api3-broken-object-property-auth",
            ruleName = "Broken Object Property Level Authorization",
            severity = RuleSeverity.High,
            category = "authorization",
            title = "Excessive data exposure in GET /users response",
            description = "The /users list endpoint returns internal fields including password_hash, last_login_ip, and internal_notes that should never be exposed to API consumers.",
            evidence = FindingEvidence(
                sourceType = "openapi_spec",
                sourcePath = "patient-portal-api.yaml",
                lineNumber = 215,
                snippet = "UserResponse:\n  properties:\n    id: {type: string}\n    email: {type: string}\n    password_hash: {type: string}\n    last_login_ip: {type: string}",
                context = "Sensitive fields exposed in public response schema",
            ),
            remediation = FindingRemediation(
                summary = "Use a dedicated response DTO that only includes fields intentionally exposed to API consumers.",
                codeExample = "class UserPublicResponse(BaseModel):\n    id: str\n    email: str\n    name: str\n    created_at: datetime",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/"),
            ),
            complianceRefs = listOf("OWASP-API3:2023", "HIPAA-164.514(b)"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-005",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "hipaa-164.312.a.2.iv",
            ruleName = "Encryption and Decryption — Data at Rest",
            severity = RuleSeverity.Medium,
            category = "encryption",
            title = "Encryption not enforced for data at rest",
            description = "Patient health records stored in the database are not encrypted at rest. The database configuration does not enable column-level or tablespace-level encryption for the PHI tables.",
            evidence = FindingEvidence(
                sourceType = "db_schema",
                sourcePath = "schema/patients.sql",
                lineNumber = 8,
                snippet = "CREATE TABLE patients (\n  id UUID PRIMARY KEY,\n  ssn VARCHAR(11),\n  diagnosis TEXT,\n  medications JSONB\n);",
                context = "No encryption directives found on PHI columns",
            ),
            remediation = FindingRemediation(
                summary = "Enable transparent data encryption (TDE) at the database level or apply column-level encryption on PHI fields using pgcrypto or equivalent.",
                codeExample = "CREATE TABLE patients (\n  id UUID PRIMARY KEY,\n  ssn BYTEA, -- store as pgcrypto-encrypted bytes\n  diagnosis BYTEA,\n  medications BYTEA\n);",
                references = listOf(
                    "https://www.hhs.gov/hipaa/for-professionals/security/guidance/index.html",
                    "https://www.postgresql.org/docs/current/pgcrypto.html",
                ),
            ),
            complianceRefs = listOf("HIPAA-164.312(a)(2)(iv)", "HIPAA-164.312(e)(2)(ii)"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-006",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "hipaa-164.312.b",
            ruleName = "Audit Controls",
            severity = RuleSeverity.High,
            category = "logging",
            title = "Audit logging missing for PHI access",
            description = "No audit logging is implemented for read or write access to patient health records. HIPAA requires that all access to ePHI is logged with user identity, timestamp, and the nature of the access.",
            evidence = FindingEvidence(
                sourceType = "codebase",
                sourcePath = "app/api/patients.py",
                lineNumber = 34,
                snippet = "@router.get('/patients/{patient_id}')\nasync def get_patient(patient_id: str, db: Session):\n    return db.query(Patient).filter(Patient.id == patient_id).first()",
                context = "No audit log call before returning PHI",
            ),
            remediation = FindingRemediation(
                summary = "Add an audit log entry on every access to PHI data including user ID, resource accessed, timestamp, and access type (read/write/delete).",
                codeExample = "@router.get('/patients/{patient_id}')\nasync def get_patient(patient_id: str, db: Session, current_user: User):\n    audit_log.record(user=current_user.id, resource='patient', resource_id=patient_id, action='read')\n    return db.query(Patient).filter(Patient.id == patient_id).first()",
                references = listOf("https://www.hhs.gov/hipaa/for-professionals/security/guidance/index.html"),
            ),
            complianceRefs = listOf("HIPAA-164.312(b)", "SOC2-CC7.2"),
            status = FindingStatus.Acknowledged,
            createdAt = MOCK_TIMESTAMP,
            comment = "Logging framework being integrated in sprint 12",
        ),
        Finding(
            id = "f-007",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api9-improper-inventory",
            ruleName = "Improper Inventory Management",
            severity = RuleSeverity.Medium,
            category = "configuration",
            title = "Undocumented internal API endpoints discovered",
            description = "AI analysis discovered 4 API endpoints reachable via the production host that are not documented in the OpenAPI spec. These endpoints may expose unintended functionality or bypass security controls.",
            evidence = FindingEvidence(
                sourceType = "codebase",
                sourcePath = "app/internal/admin_api.py",
                lineNumber = 1,
                snippet = "# Internal admin endpoints — not in OpenAPI spec\n@router.delete('/admin/users/{id}/purge')\n@router.post('/admin/reset-all-passwords')",
                context = "Endpoints registered but absent from public API specification",
            ),
            remediation = FindingRemediation(
                summary = "Either document these endpoints in the OpenAPI spec with proper security requirements, or move them behind a separate internal service not reachable from the public internet.",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa9-improper-inventory-management/"),
            ),
            complianceRefs = listOf("OWASP-API9:2023"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
        Finding(
            id = "f-008",
            sessionId = sessionId,
            scanId = "mock-scan-id",
            ruleId = "owasp-api10-unsafe-consumption",
            ruleName = "Unsafe Consumption of APIs",
            severity = RuleSeverity.Low,
            category = "input_validation",
            title = "Third-party API response not validated before use",
            description = "The application consumes responses from a third-party payment gateway and passes them directly to the database layer without validating schema or sanitizing values.",
            evidence = FindingEvidence(
                sourceType = "codebase",
                sourcePath = "app/integrations/payment.py",
                lineNumber = 67,
                snippet = "response = requests.post(PAYMENT_GATEWAY_URL, json=payload)\nresult = response.json()\ndb.session.add(Payment(**result))  # Unvalidated external data",
                context = "External API response used directly as ORM constructor input",
            ),
            remediation = FindingRemediation(
                summary = "Validate and deserialize the third-party response using a strict schema (e.g., Pydantic) before passing it to any internal layer.",
                codeExample = "response = requests.post(PAYMENT_GATEWAY_URL, json=payload)\nresult = PaymentGatewayResponse.model_validate(response.json())\ndb.session.add(Payment(id=result.transaction_id, amount=result.amount, status=result.status))",
                references = listOf("https://owasp.org/API-Security/editions/2023/en/0xa10-unsafe-consumption-of-apis/"),
            ),
            complianceRefs = listOf("OWASP-API10:2023", "SOC2-CC6.1"),
            status = FindingStatus.Open,
            createdAt = MOCK_TIMESTAMP,
        ),
    )

    companion object {
        private const val MOCK_TIMESTAMP = "2026-05-16T10:00:00Z"
    }
}
