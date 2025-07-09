from locust import HttpUser, TaskSet, task, between
import random
import datetime
import json

VITAL_TYPES = ["heart-rate", "oxygen", "blood-pressure"]


def generate_vital_sign_payload():
    vital_type = random.choice(VITAL_TYPES)
    if vital_type == "heart-rate":
        value = random.randint(50, 150)
    elif vital_type == "oxygen":
        value = random.randint(85, 99)
    else:
        value = random.randint(90, 140)

    timestamp = datetime.datetime.utcnow().isoformat() + "Z"
    return {
        "deviceId": f"D00{random.randint(1,5)}",
        "type": vital_type,
        "value": value,
        "timestamp": timestamp
    }


class PatientDataCollectorTasks(TaskSet):
    @task(2)
    def create_vital_sign(self):
        payload = generate_vital_sign_payload()
        headers = {"Content-Type": "application/json"}
        self.client.post(
            "/conjunta/2p/vital-signs",
            data=json.dumps(payload),
            headers=headers
        )

    @task(1)
    def get_vital_signs_by_device(self):
        device_id = f"D00{random.randint(1,5)}"
        self.client.get(f"/conjunta/2p/vital-signs/{device_id}")


class HealthAnalyzerTasks(TaskSet):
    @task(1)
    def get_all_alerts(self):
        self.client.get("/conjunta/2p/alerts")

    @task(1)
    def get_alerts_by_device(self):
        device_id = f"D00{random.randint(1,5)}"
        self.client.get(f"/conjunta/2p/alerts/{device_id}")


class CareNotifierTasks(TaskSet):
    @task(1)
    def get_all_notifications(self):
        self.client.get("/conjunta/2p/notifications")

    @task(1)
    def get_notifications_by_event_type(self):
        event_type = random.choice(["AlertEvent", "DailyReportEvent"])
        self.client.get(f"/conjunta/2p/notifications/type/{event_type}")

    @task(1)
    def post_mock_email(self):
        payload = {"message": "Test email notification"}
        headers = {"Content-Type": "application/json"}
        self.client.post(
            "/conjunta/2p/notifications/email",
            data=json.dumps(payload),
            headers=headers
        )

    @task(1)
    def post_mock_sms(self):
        payload = {"message": "Test SMS notification"}
        headers = {"Content-Type": "application/json"}
        self.client.post(
            "/conjunta/2p/notifications/sms",
            data=json.dumps(payload),
            headers=headers
        )

    @task(1)
    def post_mock_push(self):
        payload = {"message": "Test Push notification"}
        headers = {"Content-Type": "application/json"}
        self.client.post(
            "/conjunta/2p/notifications/push",
            data=json.dumps(payload),
            headers=headers
        )


class PatientDataCollectorUser(HttpUser):
    tasks = [PatientDataCollectorTasks]
    wait_time = between(1, 3)


class HealthAnalyzerUser(HttpUser):
    tasks = [HealthAnalyzerTasks]
    wait_time = between(1, 3)


class CareNotifierUser(HttpUser):
    tasks = [CareNotifierTasks]
    wait_time = between(1, 3)
