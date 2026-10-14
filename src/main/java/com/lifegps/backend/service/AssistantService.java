package com.lifegps.backend.service;

import com.lifegps.backend.entity.Document;
import com.lifegps.backend.entity.LifeEvent;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.DocumentRepository;
import com.lifegps.backend.repository.LifeEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Rule-based assistant. It gives guidance for common life events and
 * personalises the answer using the user's own saved documents and events.
 * (No external AI service is used, so no API key is required.)
 */
@Service
public class AssistantService {

    private final DocumentRepository documentRepository;
    private final LifeEventRepository lifeEventRepository;

    public AssistantService(DocumentRepository documentRepository,
                            LifeEventRepository lifeEventRepository) {
        this.documentRepository = documentRepository;
        this.lifeEventRepository = lifeEventRepository;
    }

    public String answer(String question, User user) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        List<Document> docs = documentRepository.findByUser(user);
        List<LifeEvent> events = lifeEventRepository.findByUserOrderByCreatedAtDesc(user);

        StringBuilder sb = new StringBuilder();

        if (has(q, "travel", "foreign", "visa", "passport", "abroad")) {
            sb.append("✈️ Foreign travel preparation\n\n• A valid passport (ideally 6+ months of validity)\n• Visa or entry permission if required\n• Flight and accommodation details\n• Travel insurance\n• Emergency contacts and copies of key documents");
            appendDocCheck(sb, docs, "passport", "visa", "insurance");
        } else if (has(q, "study", "studies", "university", "education", "higher", "college")) {
            sb.append("🎓 Higher studies preparation\n\n• Academic transcripts and certificates\n• Identity proof and passport\n• Entrance exam scores\n• Statement of purpose and recommendation letters\n• Financial documents and application deadlines");
            appendDocCheck(sb, docs, "certificate", "passport", "transcript");
        } else if (has(q, "marriage", "wedding", "married")) {
            sb.append("💍 Marriage preparation\n\n• Identity proof and age proof\n• Address proof\n• Passport-size photographs\n• Local marriage registration requirements and forms\n• Registration deadlines");
            appendDocCheck(sb, docs, "aadhaar", "age", "address");
        } else if (has(q, "house", "property", "buy", "home loan", "flat")) {
            sb.append("🏠 Buying a house\n\n• Set a budget and check loan eligibility\n• Identity, PAN, address and income proof\n• Recent bank statements\n• Verify property title and ownership records\n• Review sale agreement, taxes and registration costs");
            appendDocCheck(sb, docs, "pan", "income", "bank");
        } else if (has(q, "retire", "pension")) {
            sb.append("🌅 Retirement planning\n\n• Employment records and pension documents\n• Bank statements and insurance policies\n• Review pension eligibility and retirement benefits\n• Nominee and beneficiary details");
        } else if (has(q, "medical", "health", "hospital", "emergency")) {
            sb.append("🩺 Medical emergency readiness\n\n• Keep health insurance details accessible\n• Organise medical records and prescriptions\n• Verify emergency contacts\n• Keep identity documents handy");
            appendDocCheck(sb, docs, "insurance", "medical");
        } else if (has(q, "expiry", "expire", "deadline", "reminder", "renew")) {
            sb.append("⏰ Tracking expiry dates\n\n");
            LocalDate today = LocalDate.now();
            List<Document> soon = docs.stream()
                    .filter(d -> d.getExpiryDate() != null && !d.getExpiryDate().isBefore(today)
                            && !d.getExpiryDate().isAfter(today.plusDays(60)))
                    .sorted((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()))
                    .toList();
            List<Document> expired = docs.stream()
                    .filter(d -> d.getExpiryDate() != null && d.getExpiryDate().isBefore(today))
                    .toList();
            if (soon.isEmpty() && expired.isEmpty()) {
                sb.append("None of your saved documents expire in the next 60 days. Add expiry dates to your documents so LifeGPS can remind you.");
            } else {
                if (!expired.isEmpty()) {
                    sb.append("Already expired:\n");
                    expired.forEach(d -> sb.append("• ").append(d.getDocumentName()).append(" (").append(d.getExpiryDate()).append(")\n"));
                    sb.append("\n");
                }
                if (!soon.isEmpty()) {
                    sb.append("Expiring within 60 days:\n");
                    soon.forEach(d -> sb.append("• ").append(d.getDocumentName()).append(" (").append(d.getExpiryDate()).append(")\n"));
                }
            }
        } else if (has(q, "my events", "life event", "plan", "checklist")) {
            sb.append("📝 Your life-event plan\n\n");
            if (events.isEmpty()) {
                sb.append("You haven't saved any life events yet. Open Life Events, choose a milestone and save it with a target date.");
            } else {
                events.forEach(e -> sb.append("• ").append(e.getEventName())
                        .append(" — ").append(e.getStatus())
                        .append(e.getTargetDate() == null ? "" : " (target " + e.getTargetDate() + ")")
                        .append("\n"));
            }
        } else if (has(q, "document", "missing")) {
            sb.append("📄 Your documents\n\n");
            if (docs.isEmpty()) {
                sb.append("You have no documents saved yet. Add your ID proof, passport, insurance and finance records in the Documents page.");
            } else {
                sb.append("You have ").append(docs.size()).append(" saved document(s):\n");
                docs.forEach(d -> sb.append("• ").append(d.getDocumentName()).append(" [").append(d.getCategory()).append("]\n"));
                sb.append("\nPick a life event and I can tell you which documents to double-check.");
            }
        } else {
            sb.append("Hi ").append(user.getName() == null ? "there" : user.getName())
                    .append("! I can help with foreign travel, higher studies, marriage, buying a house, retirement, medical readiness, document expiry and your saved life events. Try asking about one of those.");
        }
        return sb.toString().trim();
    }

    private void appendDocCheck(StringBuilder sb, List<Document> docs, String... keywords) {
        sb.append("\n\n📂 From your saved documents:\n");
        for (String k : keywords) {
            boolean found = docs.stream().anyMatch(d ->
                    (d.getDocumentName() != null && d.getDocumentName().toLowerCase(Locale.ROOT).contains(k))
                            || (d.getCategory() != null && d.getCategory().toLowerCase(Locale.ROOT).contains(k)));
            sb.append(found ? "✅ " : "⚠️ Not found yet: ").append(k).append("\n");
        }
    }

    private boolean has(String q, String... words) {
        for (String w : words) if (q.contains(w)) return true;
        return false;
    }
}
