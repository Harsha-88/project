import { Module } from '@nestjs/common';
import { ScheduleModule } from '@nestjs/schedule';
import { PrismaModule } from '../prisma/prisma.module';
import { UserController } from './user.controller';
import { UserService } from './user.service';
import { OutboxRelayService } from '../outbox/outbox-relay.service';

@Module({
  imports: [PrismaModule, ScheduleModule.forRoot()],
  controllers: [UserController],
  providers: [UserService, OutboxRelayService],
})
export class UserModule {}
