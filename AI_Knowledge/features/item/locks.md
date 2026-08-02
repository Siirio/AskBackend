# Item — Feature Locks

LOCKED | One concrete variation = one Item entity | No variant tables | Item feature
LOCKED | An Item has one flat ITEM category and one canonical attributes map | Category identity is selected or explicitly created; a free-form category label is not canonical | Item, category flow
LOCKED | Item creation does not require a branch | A branch may later hold location-specific facts only | Item creation and branch association
LOCKED | Item purchase destinations belong to the Item, never a branch | A seller may publish multiple labeled customer URLs for one Item; branch context only answers where the Item is located | Item schema, cabinet editor, search projection, Proceed to Purchase
LOCKED | Verification and moderation links are not purchase destinations | A source collected to verify a Business was not supplied for customer navigation | onboarding, BusinessVerification, Item purchase destinations
LOCKED | No stock, availability-confidence, or freshness claims | Only explicit business facts may be shown | Item and search projection
