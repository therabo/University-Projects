package catering.businesslogic.recipe;

import java.math.BigDecimal;
import java.util.List;

public final class RecipeBookEntry {
    public enum Kind { RECIPE, PREPARATION }
    public enum Status { DRAFT, PUBLISHED }

    public static final class Ingredient {
        private final String name;
        private final Integer preparationId;
        private final BigDecimal amount;
        private final String unit;

        public Ingredient(String name, Integer preparationId, BigDecimal amount, String unit) {
            if ((name == null || name.isBlank()) == (preparationId == null)
                    || amount == null || amount.signum() <= 0 || unit == null || unit.isBlank()) {
                throw new IllegalArgumentException("Ingredient needs a name or preparation, positive amount and unit");
            }
            this.name = name;
            this.preparationId = preparationId;
            this.amount = amount;
            this.unit = unit;
        }

        public String getName() { return name; }
        public Integer getPreparationId() { return preparationId; }
        public BigDecimal getAmount() { return amount; }
        public String getUnit() { return unit; }
    }

    private final int id;
    private final int ownerId;
    private final String name;
    private final Kind kind;
    private final String author;
    private final String description;
    private final BigDecimal yieldAmount;
    private final String yieldUnit;
    private final Integer estimatedMinutes;
    private final Status status;
    private final List<Ingredient> ingredients;
    private final List<String> preparationSteps;
    private final List<String> serviceSteps;
    private final List<String> tags;

    public RecipeBookEntry(int id, int ownerId, String name, Kind kind, String author,
                           String description, BigDecimal yieldAmount, String yieldUnit,
                           Integer estimatedMinutes, Status status, List<Ingredient> ingredients,
                           List<String> preparationSteps, List<String> serviceSteps, List<String> tags) {
        if (name == null || name.isBlank() || kind == null || status == null
                || yieldAmount != null && yieldAmount.signum() <= 0
                || estimatedMinutes != null && estimatedMinutes <= 0
                || yieldAmount != null && (yieldUnit == null || yieldUnit.isBlank())) {
            throw new IllegalArgumentException("Invalid recipe details");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.name = name.trim();
        this.kind = kind;
        this.author = author;
        this.description = description;
        this.yieldAmount = yieldAmount;
        this.yieldUnit = yieldUnit;
        this.estimatedMinutes = estimatedMinutes;
        this.status = status;
        this.ingredients = List.copyOf(ingredients);
        this.preparationSteps = List.copyOf(preparationSteps);
        this.serviceSteps = List.copyOf(serviceSteps);
        this.tags = List.copyOf(tags);
    }

    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public Kind getKind() { return kind; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public BigDecimal getYieldAmount() { return yieldAmount; }
    public String getYieldUnit() { return yieldUnit; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public Status getStatus() { return status; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public List<String> getPreparationSteps() { return preparationSteps; }
    public List<String> getServiceSteps() { return serviceSteps; }
    public List<String> getTags() { return tags; }
}
