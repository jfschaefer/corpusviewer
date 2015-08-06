# This filter accepts the first 100 instances.

NUMBER_OF_ACCEPTED_INSTANCES = 100

instance_counter = 0

def filter(instance, interpretations):
    global instance_counter
    instance_counter += 1
    return instance_counter <= NUMBER_OF_ACCEPTED_INSTANCES

