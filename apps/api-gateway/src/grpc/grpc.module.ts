import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { AuthGrpcService } from './auth.grpc.service';
import { UserGrpcService } from './user.grpc.service';

@Module({
  imports: [
    ClientsModule.register([
      {
        name: 'AUTH_GRPC',
        transport: Transport.GRPC,
        options: {
          package: 'auth',
          protoPath: join(__dirname, '../../../../proto/auth.proto'),
          url: process.env.AUTH_SERVICE_GRPC_URL || '127.0.0.1:50051',
        },
      },
      {
        name: 'USER_GRPC',
        transport: Transport.GRPC,
        options: {
          package: 'user',
          protoPath: join(
            __dirname,
            '../../../user-service/src/proto/user.proto',
          ),
          url: process.env.USER_SERVICE_GRPC_URL || '127.0.0.1:50052',
        },
      },
    ]),
  ],
  providers: [AuthGrpcService, UserGrpcService],
  exports: [AuthGrpcService, UserGrpcService],
})
export class GrpcModule {}
