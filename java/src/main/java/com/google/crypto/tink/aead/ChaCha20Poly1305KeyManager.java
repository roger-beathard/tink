// Copyright 2017 Google Inc.
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
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTypeManager;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.proto.ChaCha20Poly1305Key;
import com.google.crypto.tink.proto.ChaCha20Poly1305KeyFormat;
import com.google.crypto.tink.proto.KeyData.KeyMaterialType;
import com.google.crypto.tink.subtle.ChaCha20Poly1305;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.subtle.Validators;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.GeneralSecurityException;

/**
 * This instance of {@code KeyManager} generates new {@code ChaCha20Poly1305} keys and produces new
 * instances of {@code ChaCha20Poly1305}.
 */
public class ChaCha20Poly1305KeyManager extends KeyTypeManager<ChaCha20Poly1305Key> {
  ChaCha20Poly1305KeyManager() {
    super(
        ChaCha20Poly1305Key.class,
        new PrimitiveFactory<Aead, ChaCha20Poly1305Key>(Aead.class) {
          @Override
          public Aead getPrimitive(ChaCha20Poly1305Key key) throws GeneralSecurityException {
            return new ChaCha20Poly1305(key.getKeyValue().toByteArray());
          }
        });
  }

  private static final int KEY_SIZE_IN_BYTES = 32;

  @Override
  public String getKeyType() {
    return "type.googleapis.com/google.crypto.tink.ChaCha20Poly1305Key";
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public KeyMaterialType keyMaterialType() {
    return KeyMaterialType.SYMMETRIC;
  }

  @Override
  public void validateKey(ChaCha20Poly1305Key key) throws GeneralSecurityException {
    Validators.validateVersion(key.getVersion(), getVersion());
    if (key.getKeyValue().size() != KEY_SIZE_IN_BYTES) {
      throw new GeneralSecurityException("invalid ChaCha20Poly1305Key: incorrect key length");
    }
  }

  @Override
  public ChaCha20Poly1305Key parseKey(ByteString byteString) throws InvalidProtocolBufferException {
    return ChaCha20Poly1305Key.parseFrom(byteString);
  }

  @Override
  public KeyFactory<ChaCha20Poly1305KeyFormat, ChaCha20Poly1305Key> keyFactory() {
    return new KeyFactory<ChaCha20Poly1305KeyFormat, ChaCha20Poly1305Key>(
        ChaCha20Poly1305KeyFormat.class) {
      @Override
      public void validateKeyFormat(ChaCha20Poly1305KeyFormat format)
          throws GeneralSecurityException {}

      @Override
      public ChaCha20Poly1305KeyFormat parseKeyFormat(ByteString byteString)
          throws InvalidProtocolBufferException {
        return ChaCha20Poly1305KeyFormat.parseFrom(byteString);
      }

      @Override
      public ChaCha20Poly1305Key createKey(ChaCha20Poly1305KeyFormat format)
          throws GeneralSecurityException {
        return ChaCha20Poly1305Key.newBuilder()
            .setVersion(getVersion())
            .setKeyValue(ByteString.copyFrom(Random.randBytes(KEY_SIZE_IN_BYTES)))
            .build();
      }
    };
  }

  public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
    Registry.registerKeyManager(new ChaCha20Poly1305KeyManager(), newKeyAllowed);
  }
}
