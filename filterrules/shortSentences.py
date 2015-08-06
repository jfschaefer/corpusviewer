# This filter accepts only sentences with at most 10 words in the string interpretation.
# Remark: Of course, this requires the existance of an interpretations called "string",
#         which has to be an object of the de.up.ling.irtg.algebra.StringAlgebra

INTERPRETATION_NAME = "string"
MAX_SENTENCE_LENGTH = 10

def filter(instance, interpretations):
    return interpretations[INTERPRETATION_NAME].size() <= MAX_SENTENCE_LENGTH

