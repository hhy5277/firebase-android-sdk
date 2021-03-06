// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.firestore;

import static com.google.firebase.firestore.testutil.IntegrationTestUtil.testCollection;
import static com.google.firebase.firestore.testutil.IntegrationTestUtil.waitFor;
import static com.google.firebase.firestore.testutil.TestUtil.expectError;
import static junit.framework.Assert.assertEquals;

import android.support.test.runner.AndroidJUnit4;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.testutil.IntegrationTestUtil;
import java.util.Date;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class POJOTest {

  public static class POJO {

    double number;
    String str;
    Date date;
    Timestamp timestamp;
    Blob blob;
    GeoPoint geoPoint;
    DocumentReference documentReference;

    public POJO() {}

    public POJO(double number, String str, DocumentReference documentReference) {
      this.number = number;
      this.str = str;
      this.documentReference = documentReference;

      // Just set default values so we can make sure they round-trip.
      this.date = new Date(123);
      this.timestamp = new Timestamp(123, 123456000);
      this.blob = Blob.fromBytes(new byte[] {3, 1, 4, 1, 5});
      this.geoPoint = new GeoPoint(3.1415, 9.2653);
    }

    public double getNumber() {
      return number;
    }

    public void setNumber(double number) {
      this.number = number;
    }

    public String getStr() {
      return str;
    }

    public void setStr(String str) {
      this.str = str;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public Timestamp getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
      this.timestamp = timestamp;
    }

    public Blob getBlob() {
      return blob;
    }

    public void setBlob(Blob blob) {
      this.blob = blob;
    }

    public GeoPoint getGeoPoint() {
      return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
      this.geoPoint = geoPoint;
    }

    public DocumentReference getDocumentReference() {
      return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
      this.documentReference = documentReference;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      POJO pojo = (POJO) o;

      if (Double.compare(pojo.number, number) != 0) {
        return false;
      }
      if (!str.equals(pojo.str)) {
        return false;
      }
      if (!date.equals(pojo.date)) {
        return false;
      }
      if (!timestamp.equals(pojo.timestamp)) {
        return false;
      }
      if (!blob.equals(pojo.blob)) {
        return false;
      }
      if (!geoPoint.equals(pojo.geoPoint)) {
        return false;
      }

      // TODO: Implement proper equality on DocumentReference.
      return documentReference.getPath().equals(pojo.documentReference.getPath());
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      temp = Double.doubleToLongBits(number);
      result = (int) (temp ^ (temp >>> 32));
      result = 31 * result + str.hashCode();
      result = 31 * result + date.hashCode();
      result = 31 * result + timestamp.hashCode();
      result = 31 * result + blob.hashCode();
      result = 31 * result + geoPoint.hashCode();
      result = 31 * result + documentReference.getPath().hashCode();
      return result;
    }
  }

  @After
  public void tearDown() {
    IntegrationTestUtil.tearDown();
  }

  @Test
  public void testWriteAndRead() {
    CollectionReference collection = testCollection();
    POJO data = new POJO(1.0, "a", collection.document());
    DocumentReference reference = waitFor(collection.add(data));
    DocumentSnapshot doc = waitFor(reference.get());
    POJO otherData = doc.toObject(POJO.class);
    assertEquals(data, otherData);
  }

  @Test
  public void testUpdate() {
    CollectionReference collection = testCollection();
    POJO data = new POJO(1.0, "a", collection.document());
    DocumentReference reference = waitFor(collection.add(data));
    DocumentSnapshot doc = waitFor(reference.get());
    POJO otherData = doc.toObject(POJO.class);
    assertEquals(data, otherData);

    otherData = new POJO(2.0, "b", data.getDocumentReference());
    waitFor(reference.set(otherData, SetOptions.mergeFields("number")));
    POJO expected = new POJO(2.0, "a", data.getDocumentReference());
    doc = waitFor(reference.get());
    assertEquals(expected, doc.toObject(POJO.class));
  }

  @Test
  public void setFieldMaskMustHaveCorrespondingValue() {
    CollectionReference collection = testCollection();
    DocumentReference reference = collection.document();

    expectError(
        () -> reference.set(new POJO(), SetOptions.mergeFields("str", "missing")),
        "Field 'missing' is specified in your field mask but not in your input data.");
  }
}
